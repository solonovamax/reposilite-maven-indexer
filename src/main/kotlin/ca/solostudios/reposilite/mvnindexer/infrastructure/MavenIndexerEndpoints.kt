package ca.solostudios.reposilite.mvnindexer.infrastructure

import ca.solostudios.reposilite.mvnindexer.MavenIndexerFacade
import com.reposilite.maven.infrastructure.MavenRoutes
import com.reposilite.web.api.ReposiliteRoute
import io.javalin.community.routing.Route
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiParam
import io.javalin.openapi.OpenApiResponse

internal class MavenIndexerEndpoints(
    private val mavenIndexerFacade: MavenIndexerFacade,
) : MavenRoutes(mavenIndexerFacade.mavenFacade) {
    @OpenApi(
        tags = ["MavenIndexer"],
        path = "/{repository}/.index/<gav>",
        methods = [HttpMethod.GET],
        pathParams = [
            OpenApiParam(name = "repository", description = "Destination repository", required = true),
            OpenApiParam(name = "gav", description = "Artifact path qualifier", required = true)
        ],
        responses = [
            OpenApiResponse(
                status = "200",
                description = "Returns 200 if the request was successful"
            ),
            OpenApiResponse(
                status = "404",
                description = "Returns 404 as a response if requested resource is not located in the current repository"
            )
        ]
    )
    private val findFile = ReposiliteRoute<Unit>("/{repository}/.index/<gav>", Route.POST) {
        accessed {
            requireRepository { repository ->
                requireGav { gav ->
                    response = mavenIndexerFacade.indexRepository(repository, gav)
                }
            }
        }
    }

    override val routes = routes(findFile)
}

