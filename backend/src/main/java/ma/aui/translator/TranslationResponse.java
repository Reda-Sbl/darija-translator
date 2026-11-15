package ma.aui.translator;

/** JSON reply: {"original": "...", "translation": "..."}. */
public class TranslationResponse {
    public String original;
    public String translation;

    public TranslationResponse() {}

    public TranslationResponse(String original, String translation) {
        this.original = original;
        this.translation = translation;
    }
}
