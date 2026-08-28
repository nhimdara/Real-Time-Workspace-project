import { createRouter, createWebHistory } from 'vue-router'
import WorkspaceView from '@/views/WorkspaceView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'workspace',
      component: WorkspaceView,
    },
    {
      path: '/:workspaceSlug',
      name: 'workspace-slug',
      component: WorkspaceView,
    },
    {
      path: '/:workspaceSlug/:pageId',
      name: 'page-view',
      component: WorkspaceView,
    },
  ],
})

export default router
