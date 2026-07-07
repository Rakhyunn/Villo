import api from './axios'

// 내 마을 조회 → { id, villageName, villageLevel, gridSize, villagerCount, nextLevelThreshold }
export const getMyVillage = () =>
  api.get('/api/v1/village').then((r) => r.data.data)

// 마을 이름 변경
export const updateVillageName = (villageName) =>
  api.put('/api/v1/village/name', { villageName }).then((r) => r.data.data)

// 주민 상점 목록 (grade 없으면 전체) → [{ id, name, grade, price, imageUrl, description }]
export const getShopVillagers = (grade) =>
  api
    .get('/api/v1/village/people', { params: grade ? { grade } : {} })
    .then((r) => r.data.data)

// 주민 영입
export const buyVillager = (villagePeopleId) =>
  api
    .post(`/api/v1/village/people/${villagePeopleId}/buy`)
    .then((r) => r.data.data)

// 내 보유 주민 목록 (배치 여부 포함) → [{ userVillagePeopleId, name, grade, imageUrl, isPlaced }]
export const getMyVillagers = () =>
  api.get('/api/v1/village/people/my').then((r) => r.data.data)

// 배치 현황 → [{ id, userVillagePeopleId, villagerName, villagerImageUrl, gridX, gridY }]
export const getPlacements = () =>
  api.get('/api/v1/village/placements').then((r) => r.data.data)

// 주민 배치 (payload: { userVillagePeopleId, gridX, gridY })
export const createPlacement = (payload) =>
  api.post('/api/v1/village/placements', payload).then((r) => r.data.data)

// 배치 위치 변경
export const updatePlacement = (placementId, payload) =>
  api
    .put(`/api/v1/village/placements/${placementId}`, payload)
    .then((r) => r.data.data)

// 배치 해제
export const deletePlacement = (placementId) =>
  api
    .delete(`/api/v1/village/placements/${placementId}`)
    .then((r) => r.data.data)
