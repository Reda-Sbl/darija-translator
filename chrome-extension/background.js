// Right-click menu -> store selected text -> open the side panel.
chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.create({
    id: "translate-darija",
    title: "Translate to Darija",
    contexts: ["selection"]
  });
  chrome.sidePanel.setPanelBehavior({ openPanelOnActionClick: true });
});

chrome.contextMenus.onClicked.addListener(async (info, tab) => {
  if (info.menuItemId !== "translate-darija") return;
  // Save first so the panel can read it even if it opens after this runs.
  await chrome.storage.session.set({ pendingText: info.selectionText });
  // open() must be called directly inside the user gesture (the click).
  chrome.sidePanel.open({ tabId: tab.id });
});
