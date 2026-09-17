import { createRouter, createWebHistory } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import EquipmentListView from '../views/EquipmentListView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: AppLayout,
      redirect: '/equipment',
      children: [
        {
          path: 'equipment',
          name: 'equipment',
          component: EquipmentListView,
          meta: {
            title: '设备管理',
          },
        },
      ],
    },
  ],
})

export default router
