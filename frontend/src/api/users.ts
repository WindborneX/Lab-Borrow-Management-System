import { request } from './http'

export interface User {
  id: number
  studentId: string
  username: string
  role: number
  createdAt: string
}

export interface UserCreatePayload {
  studentId: string
  username: string
  role?: number
}

export const getUser = (id: number) => request<User>(`/users/${id}`)

export const getUserByStudentId = (studentId: string) =>
  request<User>(`/users?studentId=${encodeURIComponent(studentId)}`)

export const createUser = (payload: UserCreatePayload) =>
  request<User>('/users', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
