package ca.solostudios.reposilite.mvnindexer.infrastructure

import ca.solostudios.reposilite.mvnindexer.MavenIndexerFacade
import ca.solostudios.reposilite.mvnindexer.util.resultAttachment
import com.reposilite.frontend.FrontendFacade
import com.reposilite.maven.Repository
import com.reposilite.maven.api.LookupRequest
import com.reposilite.maven.infrastructure.MavenRoutes
import com.reposilite.shared.ErrorResponse
import com.reposilite.shared.extensions.uri
import com.reposilite.shared.notFoundError
import com.reposilite.storage.api.DocumentInfo
import com.reposilite.storage.api.Location
import com.reposilite.token.AccessTokenIdentifier
import com.reposilite.web.api.ReposiliteRoute
import io.javalin.community.routing.Route
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiParam
import io.javalin.openapi.OpenApiResponse
import panda.std.Result
import java.io.InputStream

internal class MavenIndexerEndpoints(
    private val mavenIndexerFacade: MavenIndexerFacade,
    private val frontendFacade: FrontendFacade,
    private val compressionStrategy: String
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
    private val findFile = ReposiliteRoute<Unit>("/{repository}/.index/<gav>", Route.GET) {
        accessed {
            requireRepository { repository ->
                requireGav { gav ->
                    findFile(this?.identifier, repository, gav).peek { (stream, info) ->
                        ctx.resultAttachment(info, compressionStrategy, stream)
                    }.onError {
                        ctx.status(it.status).html(frontendFacade.createNotFoundPage(ctx.uri(), it.message))
                        mavenFacade.logger.debug("FIND | Could not find file due to $it")
                    }
                }
            }
        }
    }

    private fun findFile(
        identifier: AccessTokenIdentifier?,
        repository: Repository,
        gav: Location
    ): Result<Pair<InputStream, DocumentInfo>, ErrorResponse> {
        val request = LookupRequest(accessToken = identifier, repository = repository.name, gav = gav)

        return mavenIndexerFacade.findDetails(request)
            .flatMap { details ->
                when (details) {
                    is DocumentInfo -> mavenIndexerFacade.findData(request).map { it to details }
                    else            -> notFoundError()
                }
            }
    }

    override val routes = routes(findFile)
}

