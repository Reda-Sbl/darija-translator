package ma.aui.translator;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Principal;
import java.util.Base64;

/** HTTP Basic authentication for every request (CORS preflight excluded). */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext ctx) {
        if ("OPTIONS".equalsIgnoreCase(ctx.getMethod())) return; // preflight carries no credentials

        String header = ctx.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Basic ")) {
            try {
                String decoded = new String(Base64.getDecoder().decode(header.substring(6)), StandardCharsets.UTF_8);
                int idx = decoded.indexOf(':');
                if (idx > 0) {
                    String user = decoded.substring(0, idx);
                    String pass = decoded.substring(idx + 1);
                    if (safeEquals(user, Config.get("auth.username"))
                            && safeEquals(pass, Config.get("auth.password"))) {
                        ctx.setSecurityContext(new SecurityContext() {
                            public Principal getUserPrincipal() { return () -> user; }
                            public boolean isUserInRole(String role) { return true; }
                            public boolean isSecure() { return false; }
                            public String getAuthenticationScheme() { return BASIC_AUTH; }
                        });
                        return;
                    }
                }
            } catch (IllegalArgumentException ignored) { /* bad base64 -> 401 */ }
        }
        ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Darija Translator\"")
                .entity("{\"error\":\"Unauthorized\"}")
                .type("application/json")
                .build());
    }

    private static boolean safeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
