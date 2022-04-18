import { createRouter, createWebHashHistory } from 'vue-router'
import { queryToken } from '../store'
import home from './home'
import audit from './audit'
import editor from './editor'

import ConsoleView from '../views/home/ConsoleView.vue'

let routes = [
  {
    path: '/',
    name: 'console',
    meta: {
      authority: true
    },
    // 首页不能使用 import
    component: ConsoleView
  },
  {
    path: '/non-authority',
    name: 'non-authority',
    meta: {
      authority: true
    },
    component: () => import('@/views/NonAuthorityView.vue')
  },
  {
    path: '/about',
    name: 'about',
    meta: {
      authority: true
    },
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "about" */ '@/views/AboutView.vue')
  }
]

routes = routes.concat(home)
routes = routes.concat(audit)
routes = routes.concat(editor)

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  console.log(to)
  queryToken(to.query, router)
  const meta = to.meta

  // 判断是否允许访问
  if (meta.authority) {
    next()
  } else {
    // 不允许访问，跳转到无权限页面
    next('/non-authority')
  }
})

export default router