export interface ElementInfo {
  tagName: string
  id: string
  className: string
  textContent: string
  selector: string
  pagePath: string
  rect: {
    top: number
    left: number
    width: number
    height: number
  }
}

export interface VisualEditorOptions {
  onElementSelected?: (elementInfo: ElementInfo) => void
  onElementHover?: (elementInfo: ElementInfo) => void
}

export class VisualEditor {
  private iframe: HTMLIFrameElement | null = null
  private isEditMode = false
  private options: VisualEditorOptions

  constructor(options: VisualEditorOptions = {}) {
    this.options = options
  }

  init(iframe: HTMLIFrameElement) {
    this.iframe = iframe
  }

  enableEditMode() {
    if (!this.iframe) return
    this.isEditMode = true
    setTimeout(() => {
      this.injectEditScript()
    }, 300)
  }

  disableEditMode() {
    this.isEditMode = false
    this.sendMessageToIframe({ type: 'TOGGLE_EDIT_MODE', editMode: false })
    this.sendMessageToIframe({ type: 'CLEAR_ALL_EFFECTS' })
  }

  /** Returns the new editMode state */
  toggleEditMode(): boolean {
    if (this.isEditMode) {
      this.disableEditMode()
    } else {
      this.enableEditMode()
    }
    return this.isEditMode
  }

  syncState() {
    if (!this.isEditMode) {
      this.sendMessageToIframe({ type: 'CLEAR_ALL_EFFECTS' })
    }
  }

  clearSelection() {
    this.sendMessageToIframe({ type: 'CLEAR_SELECTION' })
  }

  onIframeLoad() {
    if (this.isEditMode) {
      setTimeout(() => this.injectEditScript(), 500)
    } else {
      setTimeout(() => this.syncState(), 500)
    }
  }

  handleIframeMessage(event: MessageEvent) {
    const { type, data } = event.data ?? {}
    if (type === 'ELEMENT_SELECTED' && data?.elementInfo) {
      this.options.onElementSelected?.(data.elementInfo)
    } else if (type === 'ELEMENT_HOVER' && data?.elementInfo) {
      this.options.onElementHover?.(data.elementInfo)
    }
  }

  private sendMessageToIframe(msg: Record<string, unknown>) {
    this.iframe?.contentWindow?.postMessage(msg, '*')
  }

  private injectEditScript() {
    if (!this.iframe) return
    const tryInject = () => {
      try {
        const doc = this.iframe!.contentDocument
        if (!doc) { setTimeout(tryInject, 100); return }
        if (doc.getElementById('visual-edit-script')) {
          this.sendMessageToIframe({ type: 'TOGGLE_EDIT_MODE', editMode: true })
          return
        }
        const script = doc.createElement('script')
        script.id = 'visual-edit-script'
        script.textContent = this.generateEditScript()
        doc.head.appendChild(script)
      } catch {
        // cross-origin or not ready — silently ignore
      }
    }
    tryInject()
  }

  private generateEditScript(): string {
    return `
(function() {
  let isEditMode = true;
  let currentHoverElement = null;
  let currentSelectedElement = null;
  let eventListenersAdded = false;

  function injectStyles() {
    if (document.getElementById('edit-mode-styles')) return;
    const style = document.createElement('style');
    style.id = 'edit-mode-styles';
    style.textContent = \`
      .edit-hover {
        outline: 2px dashed #1890ff !important;
        outline-offset: 2px !important;
        cursor: crosshair !important;
        transition: outline 0.15s ease !important;
      }
      .edit-selected {
        outline: 3px solid #52c41a !important;
        outline-offset: 2px !important;
      }
    \`;
    document.head.appendChild(style);
  }

  function generateSelector(el) {
    const path = [];
    let cur = el;
    while (cur && cur !== document.body) {
      let sel = cur.tagName.toLowerCase();
      if (cur.id) { sel += '#' + cur.id; path.unshift(sel); break; }
      const classes = (cur.className || '').split(' ').filter(c => c && !c.startsWith('edit-'));
      if (classes.length) sel += '.' + classes.join('.');
      const siblings = Array.from(cur.parentElement?.children || []);
      sel += ':nth-child(' + (siblings.indexOf(cur) + 1) + ')';
      path.unshift(sel);
      cur = cur.parentElement;
    }
    return path.join(' > ');
  }

  function getElementInfo(el) {
    const rect = el.getBoundingClientRect();
    return {
      tagName: el.tagName,
      id: el.id || '',
      className: (el.className || '').split(' ').filter(c => c && !c.startsWith('edit-')).join(' '),
      textContent: (el.textContent || '').trim().substring(0, 100),
      selector: generateSelector(el),
      pagePath: window.location.search + window.location.hash,
      rect: { top: rect.top, left: rect.left, width: rect.width, height: rect.height }
    };
  }

  function clearHover() {
    if (currentHoverElement) { currentHoverElement.classList.remove('edit-hover'); currentHoverElement = null; }
  }

  function clearSelected() {
    document.querySelectorAll('.edit-selected').forEach(el => el.classList.remove('edit-selected'));
    currentSelectedElement = null;
  }

  function addListeners() {
    if (eventListenersAdded) return;
    document.body.addEventListener('mouseover', (e) => {
      if (!isEditMode) return;
      const t = e.target;
      if (t === currentHoverElement || t === currentSelectedElement) return;
      if (t === document.body || t === document.documentElement) return;
      if (t.tagName === 'SCRIPT' || t.tagName === 'STYLE') return;
      clearHover();
      t.classList.add('edit-hover');
      currentHoverElement = t;
    }, true);

    document.body.addEventListener('mouseout', (e) => {
      if (!isEditMode) return;
      if (!e.relatedTarget || !e.target.contains(e.relatedTarget)) clearHover();
    }, true);

    document.body.addEventListener('click', (e) => {
      if (!isEditMode) return;
      e.preventDefault(); e.stopPropagation();
      const t = e.target;
      if (t === document.body || t === document.documentElement) return;
      if (t.tagName === 'SCRIPT' || t.tagName === 'STYLE') return;
      clearSelected(); clearHover();
      t.classList.add('edit-selected');
      currentSelectedElement = t;
      try { window.parent.postMessage({ type: 'ELEMENT_SELECTED', data: { elementInfo: getElementInfo(t) } }, '*'); } catch {}
    }, true);

    eventListenersAdded = true;
  }

  function showTip() {
    if (document.getElementById('edit-tip')) return;
    const tip = document.createElement('div');
    tip.id = 'edit-tip';
    tip.textContent = 'Edit mode — hover to inspect, click to select';
    tip.style.cssText = 'position:fixed;top:16px;right:16px;background:#1677ff;color:#fff;padding:10px 16px;border-radius:6px;font-size:13px;z-index:9999;box-shadow:0 4px 12px rgba(0,0,0,.2);';
    document.body.appendChild(tip);
    setTimeout(() => tip.remove(), 3000);
  }

  window.addEventListener('message', (e) => {
    const { type, editMode } = e.data || {};
    switch (type) {
      case 'TOGGLE_EDIT_MODE':
        isEditMode = editMode;
        if (isEditMode) { injectStyles(); addListeners(); showTip(); }
        else { clearHover(); clearSelected(); }
        break;
      case 'CLEAR_SELECTION':
        clearSelected();
        break;
      case 'CLEAR_ALL_EFFECTS':
        isEditMode = false; clearHover(); clearSelected();
        const tip = document.getElementById('edit-tip');
        if (tip) tip.remove();
        break;
    }
  });

  injectStyles();
  addListeners();
  showTip();
})();
`
  }
}
