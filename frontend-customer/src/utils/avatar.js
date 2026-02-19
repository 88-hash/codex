const makeAvatarSvg = (background, label, color = '#111111') => {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="128" height="128" viewBox="0 0 128 128">
      <rect width="128" height="128" rx="28" fill="${background}" />
      <circle cx="64" cy="64" r="54" fill="rgba(255,255,255,0.22)" />
      <text x="64" y="75" text-anchor="middle" font-size="50" font-family="Apple Color Emoji, Segoe UI Emoji, Noto Color Emoji, sans-serif" fill="${color}">
        ${label}
      </text>
    </svg>
  `
  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`
}

export const PRESET_AVATARS = [
  { id: 'cookie', name: '饼干', url: makeAvatarSvg('#FFD700', '🍪') },
  { id: 'drink', name: '饮料', url: makeAvatarSvg('#00BFFF', '🥤') },
  { id: 'choco', name: '巧克力', url: makeAvatarSvg('#FF69B4', '🍫') },
  { id: 'candy', name: '糖果', url: makeAvatarSvg('#00E676', '🍬') },
  { id: 'joy', name: '快乐', url: makeAvatarSvg('#FFB347', '😋') },
  { id: 'tea', name: '下午茶', url: makeAvatarSvg('#7DD3FC', '🧋') },
  { id: 'snack', name: '零食', url: makeAvatarSvg('#FDBA74', '🍿') },
  { id: 'shop', name: '购物', url: makeAvatarSvg('#C4B5FD', '🛍️') }
]

export const DEFAULT_PRESET_AVATAR = PRESET_AVATARS[0].url
