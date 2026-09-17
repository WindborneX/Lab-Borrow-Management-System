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

export const createBorrowApplication = (payload: BorrowApplicationPayload) =>
  request<BorrowRecord>('/borrow-applications', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
