package ca.solostudios.reposilite.mvnindexer.infrastructure

import ca.solostudios.reposilite.mvnindexer.MavenIndexerFacade
import com.reposilite.web.api.ReposiliteRoute
import io.javalin.community.routing.Route
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiParam
import io.javalin.openapi.OpenApiResponse

internal class MavenIndexerApiEndpoints(
    private val mavenIndexerFacade: MavenIndexerFacade,
) : MavenIndexerRoutes(mavenIndexerFacade.mavenFacade) {
    @OpenApi(
        tags = ["MavenIndexer"],
        path = "/api/maven-indexer/{repository}/index/<gav>",
        methods = [HttpMethod.POST],
        pathParams = [
            OpenApiParam(name = "repository", description = "Destination repository", required = false, allowEmptyValue = true),
            OpenApiParam(name = "gav", description = "Artifact path qualifier", required = false, allowEmptyValue = true)
        ],
        responses = [
            OpenApiResponse(status = "200", description = "Returns 200 if the request was successful"),
            OpenApiResponse(status = "403", description = "Returns 403 for invalid credentials"),
        ]
    )
    private val indexRepositoryGav = ReposiliteRoute<Unit>("/api/maven-indexer/{repository}/index/<gav>", Route.POST) {
        managerOnly {
            println(ctx.path())
            requireRepository { repository ->
                optionalGav { gav ->
                    response = mavenIndexerFacade.indexRepository(repository, gav)
                }
            }
        }
    }

    @OpenApi(
        tags = ["MavenIndexer"],
        path = "/api/maven-indexer/{repository}/index",
        methods = [HttpMethod.POST],
        pathParams = [
            OpenApiParam(name = "repository", description = "Destination repository", required = false, allowEmptyValue = true),
        ],
        responses = [
            OpenApiResponse(status = "200", description = "Returns 200 if the request was successful"),
            OpenApiResponse(status = "403", description = "Returns 403 for invalid credentials"),
        ]
    )
    private val indexRepository = ReposiliteRoute<Unit>("/api/maven-indexer/{repository}/index", Route.POST) {
        managerOnly {
            println(ctx.path())
            requireRepository { repository ->
                response = mavenIndexerFacade.indexRepository(repository)
            }
        }
    }

    @OpenApi(
        tags = ["MavenIndexer"],
        path = "/api/maven-indexer/index-all/<gav>",
        methods = [HttpMethod.POST],
        pathParams = [
            OpenApiParam(name = "gav", description = "Artifact path qualifier", required = true, allowEmptyValue = true)
        ],
        responses = [
            OpenApiResponse(status = "200", description = "Returns 200 if the request was successful"),
            OpenApiResponse(status = "403", description = "Returns 403 for invalid credentials"),
        ]
    )
    private val indexGav = ReposiliteRoute<Unit>("/api/maven-indexer/index-all/<gav>", Route.POST) {
        managerOnly {
            optionalGav { gav ->
                response = mavenIndexerFacade.indexAllRepositories(gav)
            }
        }
    }

    @OpenApi(
        tags = ["MavenIndexer"],
        path = "/api/maven-indexer/index-all",
        methods = [HttpMethod.POST],
        responses = [
            OpenApiResponse(status = "200", description = "Returns 200 if the request was successful"),
            OpenApiResponse(status = "403", description = "Returns 403 for invalid credentials"),
        ]
    )
    private val index = ReposiliteRoute<Unit>("/api/maven-indexer/index-all", Route.POST) {
        managerOnly {
            response = mavenIndexerFacade.indexAllRepositories()
        }
    }

    // @OpenApi(
    //     tags = ["MavenIndexer"],
    //     path = "/api/maven-indexer/{repository}/<gav>/index",
    //     methods = [HttpMethod.DELETE],
    //     pathParams = [
    //         OpenApiParam(name = "repository", description = "Destination repository", required = false),
    //         OpenApiParam(name = "gav", description = "Artifact path qualifier", required = true, allowEmptyValue = true)
    //     ],
    //     responses = [
    //         OpenApiResponse(status = "200", description = "Returns 200 if the request was successful"),
    //         OpenApiResponse(status = "403", description = "Returns 403 for invalid credentials"),
    //     ]
    // )
    // private val purge = ReposiliteRoute("/api/maven-indexer/{repository}/<gav>/index", Route.DELETE) {
    //     managerOnly {
    //         optionalRepository { repository ->
    //             optionalGav { gav ->
    //                 response = if (repository == null)
    //                     mavenIndexerFacade.purgeAllRepositories(gav)
    //                 else
    //                     mavenIndexerFacade.purgeRepository(repository, gav)
    //             }
    //         }
    //     }
    // }

    override val routes = routes(index, indexRepository)
}

