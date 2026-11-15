# Darija Translator (Mini Project 2)

LLM-powered REST service that translates text to Moroccan Arabic (Darija).

```
darija-translator/
  backend/           Java 21 + Maven, Jersey (Jakarta REST) on embedded Grizzly
  chrome-extension/  Manifest V3, side panel, right-click menu
  python-client/     CLI client
  (php-client/, mobile-app/ : coming next)
```

## Run the backend
```bash
cd backend
export GEMINI_API_KEY=your_key_here     # free key from https://aistudio.google.com/apikey
mvn clean package
java -jar target/darija-translator-1.0.0.jar
```
> **Important:** `export` only works in the terminal window where you typed it. If you open a new
> window or restart your computer, run the `export` line again before `java -jar ...`.
> Never commit your key to GitHub.

Optional: `export GEMINI_MODEL=<model name>` and `export GEMINI_FALLBACK_MODEL=<backup model name>`
(see https://aistudio.google.com for current model names).

Credentials default to `reda` / `changeme` (edit `src/main/resources/application.properties`
or set `AUTH_USERNAME` / `AUTH_PASSWORD`).

## Test with cURL
```bash
curl -u reda:changeme -H "Content-Type: application/json" \
  -d '{"text":"Good morning, how are you?"}' http://localhost:8080/api/translate
# without credentials -> 401
curl -i http://localhost:8080/api/translate?text=Hello
```

## Chrome extension
1. Open `chrome://extensions`, enable Developer mode, click **Load unpacked**, pick `chrome-extension/`.
2. Select text on any page, right-click, **Translate to Darija**. The side panel opens and translates.

## Python client
```bash
python python-client/translate.py "Where is the train station?"
```

## Troubleshooting
| Error | Meaning | Fix |
|---|---|---|
| `GEMINI_API_KEY is not configured` | Key not set in this terminal | Run `export GEMINI_API_KEY=...` then start the server again |
| `API key not valid` | Wrong or placeholder key | Create a real key at aistudio.google.com/apikey |
| `404 ... model no longer available` | Old model name | Set `GEMINI_MODEL` to a current model |
| `503` / `request timed out` | Google is overloaded | Wait and retry (the server retries 3 times automatically) |
| `401 Unauthorized` | Missing/wrong username or password | Use `-u reda:changeme` |
