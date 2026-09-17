import { request } from './http'

export interface BorrowApplicationPayload {
  equipmentId: number
  userId: number
  expectReturnTime?: string
}

export interface BorrowRecord {
  id: number
  equipmentId: number
  userId: number
  borrowTime: string
  expectReturnTime: string | null
  actualReturnTime: string | null
  status: number
}

export interface BorrowRecordQuery {
  userId?: number
  status?: number
}

export const createBorrowApplication = (payload: BorrowApplicationPayload) =>
  request<BorrowRecord>('/borrow-applications', {
    method: 'POST',
    body: JSON.stringify(payload),
  })

export const listBorrowRecords = (query: BorrowRecordQuery) => {
  const params = new URLSearchParams()

  if (query.userId !== undefined) {
    params.set('userId', String(query.userId))
  }
  if (query.status !== undefined) {
    params.set('status', String(query.status))
  }

  const queryString = params.toString()
  return request<BorrowRecord[]>(
    `/borrow-records${queryString ? `?${queryString}` : ''}`,
  )
}

export const returnEquipment = (recordId: number, userId: number) =>
  request<BorrowRecord>(`/borrow-records/${recordId}/return`, {
    method: 'POST',
    body: JSON.stringify({ userId }),
  })
