export function normalizeUrlEntities(value) {
  if (typeof value !== 'string') return value;

  let normalized = value;
  for (let i = 0; i < 3; i += 1) {
    const next = normalized
      .replaceAll(/\\u0026/gi, '&')
      .replaceAll(/&#x26;/gi, '&')
      .replaceAll(/&#38;/gi, '&')
      .replaceAll(/&amp;/gi, '&');
    if (next === normalized) break;
    normalized = next;
  }

  return normalized;
}

export function normalizeHtmlImageSrc(html) {
  if (!html || typeof html !== 'string') return html || '';

  return html.replaceAll(
    /(<img\b[^>]*?\bsrc\s*=\s*)(["'])([^"']+)\2/gi,
    (match, prefix, quote, src) =>
      `${prefix}${quote}${normalizeUrlEntities(src)}${quote}`,
  );
}
