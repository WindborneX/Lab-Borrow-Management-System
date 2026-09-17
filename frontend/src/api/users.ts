import { request } from './http'

export interface User {
  id: number
  studentId: string
  username: string
  role: number
  createdAt: string
}

export const getUserByStudentId = (studentId: string) =>
  request<User>(`/users?studentId=${encodeURIComponent(studentId)}`)
