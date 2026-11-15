package ma.aui.translator;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** The REST endpoint: POST /api/translate */
@Path("/translate")
@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
public class TranslatorResource {

    private final GeminiService gemini = new GeminiService();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response translate(TranslationRequest request) {
        if (request == null || request.text == null || request.text.isBlank()) {
            return error(Response.Status.BAD_REQUEST, "Field 'text' is required");
        }
        try {
            String result = gemini.translate(request.text, request.source);
            return Response.ok(new TranslationResponse(request.text, result)).build();
        } catch (IllegalStateException e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Interrupted");
        } catch (Exception e) {
            return error(Response.Status.BAD_GATEWAY, e.getMessage());
        }
    }

    /** Simple GET for quick browser/cURL tests: /api/translate?text=Hello */
    @GET
    public Response translateGet(@jakarta.ws.rs.QueryParam("text") String text) {
        TranslationRequest r = new TranslationRequest();
        r.text = text;
        return translate(r);
    }

    private Response error(Response.Status status, String message) {
        return Response.status(status).entity(java.util.Map.of("error", message)).build();
    }
}
