export function parseM3U(content) {
  const lines = content.split('\n');
  const items = [];
  let current = null;

  for (const rawLine of lines) {
    const line = rawLine.trim();

    if (line.startsWith('#EXTINF')) {
      current = {
        name: line.split(',').pop() || 'Chaîne sans nom',
        url: '',
        type: 'live',
      };
    } else if (line.startsWith('http') && current) {
      current.url = line;
      items.push(current);
      current = null;
    }
  }

  return items;
}

export function buildXtreamApiUrl(server, username, password, action) {
  const cleanServer = server.replace(/\/$/, '');
  return `${cleanServer}/player_api.php?username=${username}&password=${password}&action=${action}`;
}
