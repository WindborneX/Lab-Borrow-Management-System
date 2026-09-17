import { request } from './http'

export interface Equipment {
  id: number
  name: string
  category: string | null
  status: number
  description: string | null
  createdAt: string
}

export interface EquipmentQuery {
  name?: string
  category?: string
  status?: number
}

export interface EquipmentCreatePayload {
  name: string
  category?: string
  description?: string
}

export const listEquipment = (query: EquipmentQuery) => {
  const params = new URLSearchParams()

  if (query.name) {
    params.set('name', query.name)
  }
  if (query.category) {
    params.set('category', query.category)
  }
  if (query.status !== undefined) {
    params.set('status', String(query.status))
  }

  const queryString = params.toString()
  return request<Equipment[]>(`/equipment${queryString ? `?${queryString}` : ''}`)
}

export const getEquipment = (id: number) =>
  request<Equipment>(`/equipment/${id}`)

export const createEquipment = (payload: EquipmentCreatePayload) =>
  request<Equipment>('/equipment', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
