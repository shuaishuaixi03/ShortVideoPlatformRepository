import Vue from 'vue';
import VueRouter from 'vue-router'
Vue.use(VueRouter)

const routes = [
  {
    path: '/',
		redirect: '/home-page'
  },
  {
    path: '/test',
		name: 'test',
		component: () => import(/* webpackChunkName: "about" */ '../views/test/test.vue')
  },
  {
    path: '/home-page',
    name: 'home-page',
    component: () => import(/* webpackChunkName: "about" */ '../views/home_page/home_page.vue')
  },
  {
    path: '/register',
    name: 'register',
    component: () => import(/* webpackChunkName: "about" */ '../views/register/index.vue')
  },
  {
    path: '/login',
    name: 'login',
    component: () => import(/* webpackChunkName: "about" */ '../views/login/index.vue')
  },
  {
    path: '/personal',
    name: 'personal',
    component: () => import(/* webpackChunkName: "about" */ '../views/personal/index.vue')
  },
  {
    path: '/search-result',
    name: 'search-result',
    component: () => import(/* webpackChunkName: "about" */ '../views/search/index.vue')
  },
  {
    path: '/follows',
    name: 'follows',
    component: () => import(/* webpackChunkName: "about" */ '../views/follow/index.vue')
  },
  {
    path: '/moments',
    name: 'moments',
    component: () => import(/* webpackChunkName: "about" */ '../views/moments/index.vue')
  },
  {
    path: '/post-video',
    name: 'post-video',
    component: () => import(/* webpackChunkName: "about" */ '../views/moments/video.vue')
  },
  {
    path: '/video-detail',
    name: 'video-detail',
    component: () => import(/* webpackChunkName: "about" */ '../views/video/video_detail.vue')
  },
  {
    path: '/mask-test',
    name: 'mask-test',
    component: () => import(/* webpackChunkName: "about" */ '../views/maskTest.vue')
  },
	
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.VUE_APP_BASE_ROUTE,
  routes
})

export default router
