package ca.solostudios.reposilite.mvnindexer.infrastructure

import ca.solostudios.reposilite.mvnindexer.MavenIndexerFacade
import com.reposilite.maven.infrastructure.MavenRoutes
import com.reposilite.web.api.ReposiliteRoute
import com.reposilite.web.routing.RouteMethod.GET
import io.javalin.http.ContentType
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiParam
import io.javalin.openapi.OpenApiResponse
import panda.std.Result

internal class MavenIndexerSearchEndpoints(
    private val mavenIndexerFacade: MavenIndexerFacade,
                                          ) : MavenRoutes(mavenIndexerFacade.mavenFacade) {

    @OpenApi(
            tags = ["MavenIndexer"],
            path = "/api/maven-indexer/{repository}/index",
            methods = [HttpMethod.GET], // TODO: Change
            pathParams = [
                OpenApiParam(
                        name = "repository", description = "Repository to index",
                        required = true
                            ),
            ],
            responses = [
                OpenApiResponse(
                        "200",
                        content = [OpenApiContent(from = String::class, type = ContentType.PLAIN)],
                        description = ""
                               ),
            ]
            )
    private val index = ReposiliteRoute<Any>("/api/maven-indexer/{repository}/index", GET) {
//        authorized { // TODO
        requireRepository { repository ->
            mavenIndexerFacade.indexRepository(repository)
            response = Result.ok("ok")
        }
//        }
    }

    override val routes = routes(index)
}
