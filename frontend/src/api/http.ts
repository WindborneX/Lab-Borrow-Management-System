interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export class ApiError extends Error {
  readonly code: number

  constructor(code: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

export const request = async <T>(
  path: string,
  init?: RequestInit,
): Promise<T> => {
  const response = await fetch(`/api/v1${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
  })

  const text = await response.text()
  let payload: ApiResult<T> | undefined

  if (text) {
    try {
      payload = JSON.parse(text) as ApiResult<T>
    } catch {
      payload = undefined
    }
  }

  if (!payload || !response.ok || payload.code !== 200) {
    throw new ApiError(
      payload?.code ?? response.status,
      payload?.message || `请求失败（HTTP ${response.status}）`,
    )
  }

  return payload.data
}
