package ma.aui.translator;

/** JSON body: {"text": "Hello", "source": "English"} (source is optional). */
public class TranslationRequest {
    public String text;
    public String source;

    public TranslationRequest() {}
}
