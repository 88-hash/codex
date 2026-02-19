const DEFAULT_BACKEND_URL = 'http://localhost:8080'
const DEFAULT_PLACEHOLDER = '/placeholder.svg'

const ABSOLUTE_URL_RE = /^(?:[a-z]+:)?\/\//i
const DATA_OR_BLOB_RE = /^(data:|blob:)/i

const trimTrailingSlash = (value) => value.replace(/\/+$/, '')

export const getBackendBaseUrl = () => {
  const backendEnv = import.meta.env.VITE_BACKEND_URL
  const apiEnv = import.meta.env.VITE_API_BASE_URL
  const preferred = (backendEnv || apiEnv || DEFAULT_BACKEND_URL).trim()
  const normalized = trimTrailingSlash(preferred)

  // If API base URL is provided, map image paths to backend origin.
  if (normalized.endsWith('/api')) {
    return normalized.slice(0, -4)
  }

  return normalized || DEFAULT_BACKEND_URL
}

export const normalizeImageUrl = (value) => {
  if (!value || typeof value !== 'string') {
    return DEFAULT_PLACEHOLDER
  }

  const imagePath = value.trim()
  if (!imagePath) {
    return DEFAULT_PLACEHOLDER
  }

  if (DATA_OR_BLOB_RE.test(imagePath) || ABSOLUTE_URL_RE.test(imagePath)) {
    return imagePath
  }

  const backendBase = getBackendBaseUrl()
  if (imagePath.startsWith('/')) {
    return `${backendBase}${imagePath}`
  }

  return `${backendBase}/${imagePath}`
}

export const getImageUrl = (value) => normalizeImageUrl(value)

export const handleImageError = (event) => {
  const target = event?.target
  if (!target) return

  if (target.dataset.fallbackApplied === '1') {
    target.style.visibility = 'hidden'
    return
  }

  target.dataset.fallbackApplied = '1'
  target.src = DEFAULT_PLACEHOLDER
}
