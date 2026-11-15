const $ = (id) => document.getElementById(id);
const DEFAULTS = { url: "http://localhost:8080/api/translate", user: "reda", pass: "changeme" };

async function getSettings() {
  const s = await chrome.storage.local.get(["url", "user", "pass"]);
  return { ...DEFAULTS, ...s };
}

async function translate() {
  const text = $("input").value.trim();
  if (!text) return;
  const { url, user, pass } = await getSettings();
  $("status").textContent = "Translating...";
  $("result").textContent = "";
  try {
    const res = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": "Basic " + btoa(`${user}:${pass}`)
      },
      body: JSON.stringify({ text })
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.error || `HTTP ${res.status}`);
    $("result").textContent = data.translation;
    $("status").textContent = "";
  } catch (e) {
    $("status").textContent = "Error: " + e.message;
  }
}

// Load text saved by the context menu, and react to later selections while panel is open.
async function consumePending() {
  const { pendingText } = await chrome.storage.session.get("pendingText");
  if (pendingText) {
    $("input").value = pendingText;
    await chrome.storage.session.remove("pendingText");
    translate();
  }
}
chrome.storage.onChanged.addListener((changes, area) => {
  if (area === "session" && changes.pendingText?.newValue) consumePending();
});
consumePending();

// Read aloud (Arabic voice if the browser has one)
$("speak").onclick = () => {
  const text = $("result").textContent;
  if (!text) return;
  const u = new SpeechSynthesisUtterance(text);
  u.lang = "ar-MA";
  speechSynthesis.cancel();
  speechSynthesis.speak(u);
};

// Voice input (Web Speech API; may not work in every Chrome build/side panel)
$("mic").onclick = () => {
  const SR = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SR) { $("status").textContent = "Speech recognition not supported here."; return; }
  const rec = new SR();
  rec.lang = "en-US";
  rec.onresult = (e) => { $("input").value = e.results[0][0].transcript; translate(); };
  rec.onerror = (e) => { $("status").textContent = "Mic error: " + e.error; };
  rec.start();
  $("status").textContent = "Listening...";
};

$("translate").onclick = translate;
$("save").onclick = async () => {
  await chrome.storage.local.set({ url: $("url").value, user: $("user").value, pass: $("pass").value });
  $("status").textContent = "Saved.";
};
getSettings().then((s) => { $("url").value = s.url; $("user").value = s.user; $("pass").value = s.pass; });
