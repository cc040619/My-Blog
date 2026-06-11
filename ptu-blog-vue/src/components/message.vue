<!-- 留言评论模块 -->
<template>
    <div class="tmsgBox" ref="tmsgBox">
        <div v-if="haslogin" class="tmsg-respond" ref="respondBox">
            <h3>
                发表评论
                <small v-show="isRespond" class="tcolorm" @click="removeRespond">取消回复</small>
            </h3>

            <form class="">
                <el-input
                    type="textarea"
                    :rows="2"
                    placeholder="说点什么呢``"
                    v-model="textarea">
                </el-input>

                <div :class="pBody ? 'OwO' : 'OwO OwO-open'">
                    <div class="OwO-logo" @click="pBody = !pBody">
                        <span>OwO表情</span>
                    </div>

                    <div class="OwO-body">
                        <ul class="OwO-items OwO-items-show">
                            <li
                                class="OwO-item"
                                v-for="(oitem, index) in OwOlist"
                                :key="'oitem' + index"
                                @click="choseEmoji(oitem.title)">
                                <img :src="'static/img/emot/image/' + oitem.url" alt="">
                            </li>
                        </ul>

                        <div class="OwO-bar">
                            <ul class="OwO-packages">
                                <li class="OwO-package-active">Emoji</li>
                            </ul>
                        </div>
                    </div>
                </div>

                <el-row class="tmsg-r-info">
                    <el-col :span="24" class="info-submit">
                        <p class="tcolors-bg" @click="sendMsg">{{ sendTip }}</p>
                    </el-col>
                </el-row>
            </form>
        </div>

        <!-- 未登录：显示登录提示 -->
        <div v-else class="tmsg-login-tip">
            <p>登录后才能发表评论哦~</p>
            <el-button type="primary" size="small" @click="goLogin">前往登录</el-button>
        </div>

        <div class="tmsg-comments" ref="listDom">
            <a href="#" class="tmsg-comments-tip">
                活捉 {{ commentList ? commentList.length : 0 }} 条
            </a>

            <div class="tmsg-commentshow">
                <ul class="tmsg-commentlist">
                    <li
                        class="tmsg-c-item"
                        v-for="(item, index) in commentList"
                        :key="'common' + index">
                        <article class="">
                            <header>
                                <img :src="getAvatar(item.username)" :onerror="$store.state.errorImg">

                                <div class="i-name">
                                    {{ item.username }}
                                </div>

                                <div class="i-time">
                                    <time>{{ item.createTime }}</time>
                                </div>
                            </header>

                            <section>
                                <p v-html="analyzeEmoji(item.content)">
                                    {{ analyzeEmoji(item.content) }}
                                </p>

                                <div
                                    v-if="haslogin"
                                    class="tmsg-replay"
                                    @click="respondMsg(item.id, item.id, item.createBy)">
                                    回复
                                </div>
                            </section>
                        </article>

                        <ul
                            v-show="item.children"
                            class="tmsg-commentlist"
                            style="padding-left:60px;">
                            <li
                                class="tmsg-c-item"
                                v-for="(citem, cindex) in item.children"
                                :key="'citem' + cindex">
                                <article class="">
                                    <header>
                                        <img :src="getAvatar(citem.username)" :onerror="$store.state.errorImg">

                                        <div class="i-name">
                                            {{ citem.username }}
                                            <span>回复</span>
                                            {{ citem.toCommentUserName }}
                                        </div>

                                        <div class="i-time">
                                            <time>{{ citem.createTime }}</time>
                                        </div>
                                    </header>

                                    <section>
                                        <p v-html="analyzeEmoji(citem.content)">
                                            {{ citem.content }}
                                        </p>

                                        <div
                                            v-if="haslogin"
                                            class="tmsg-replay"
                                            @click="respondMsg(item.id, citem.id, citem.createBy)">
                                            回复
                                        </div>
                                    </section>
                                </article>
                            </li>
                        </ul>
                    </li>
                </ul>

                <h1 v-show="hasMore" class="tcolors-bg" @click="addMoreFun">查看更多</h1>
                <h1 v-show="!hasMore" class="tcolors-bg">没有更多</h1>
            </div>
        </div>
    </div>
</template>

<script>
import { sendComment, getArticleComment, getLinkComment } from '../api/comment.js'
import { getToken } from '../utils/auth.js'

export default {
    data() {
        return {
            respondBox: '',
            listDom: '',
            tmsgBox: '',

            queryParams: {
                pageNum: 1,
                pageSize: 10,
                articleId: 0
            },

            isRespond: false,
            textarea: '',
            pBody: true,
            commentList: [],
            aid: 0,
            hasMore: false,
            haslogin: false,
            userId: '',
            type: 0,
            leavePid: '',
            pid: '',
            rootId: -1,
            toCommentId: -1,
            toCommentUserId: -1,
            sendTip: '发送~',

            OwOlist: [
                { title: '微笑', url: 'weixiao.gif' },
                { title: '嘻嘻', url: 'xixi.gif' },
                { title: '哈哈', url: 'haha.gif' },
                { title: '可爱', url: 'keai.gif' },
                { title: '可怜', url: 'kelian.gif' },
                { title: '挖鼻', url: 'wabi.gif' },
                { title: '吃惊', url: 'chijing.gif' },
                { title: '害羞', url: 'haixiu.gif' },
                { title: '挤眼', url: 'jiyan.gif' },
                { title: '闭嘴', url: 'bizui.gif' },
                { title: '鄙视', url: 'bishi.gif' },
                { title: '爱你', url: 'aini.gif' },
                { title: '泪', url: 'lei.gif' },
                { title: '偷笑', url: 'touxiao.gif' },
                { title: '亲亲', url: 'qinqin.gif' },
                { title: '生病', url: 'shengbing.gif' },
                { title: '太开心', url: 'taikaixin.gif' },
                { title: '白眼', url: 'baiyan.gif' },
                { title: '右哼哼', url: 'youhengheng.gif' },
                { title: '左哼哼', url: 'zuohengheng.gif' },
                { title: '嘘', url: 'xu.gif' },
                { title: '衰', url: 'shuai.gif' },
                { title: '吐', url: 'tu.gif' },
                { title: '哈欠', url: 'haqian.gif' },
                { title: '抱抱', url: 'baobao.gif' },
                { title: '怒', url: 'nu.gif' },
                { title: '疑问', url: 'yiwen.gif' },
                { title: '馋嘴', url: 'chanzui.gif' },
                { title: '拜拜', url: 'baibai.gif' },
                { title: '思考', url: 'sikao.gif' },
                { title: '汗', url: 'han.gif' },
                { title: '困', url: 'kun.gif' },
                { title: '睡', url: 'shui.gif' },
                { title: '钱', url: 'qian.gif' },
                { title: '失望', url: 'shiwang.gif' },
                { title: '酷', url: 'ku.gif' },
                { title: '色', url: 'se.gif' },
                { title: '哼', url: 'heng.gif' },
                { title: '鼓掌', url: 'guzhang.gif' },
                { title: '晕', url: 'yun.gif' },
                { title: '悲伤', url: 'beishang.gif' },
                { title: '抓狂', url: 'zhuakuang.gif' },
                { title: '黑线', url: 'heixian.gif' },
                { title: '阴险', url: 'yinxian.gif' },
                { title: '怒骂', url: 'numa.gif' },
                { title: '互粉', url: 'hufen.gif' },
                { title: '书呆子', url: 'shudaizi.gif' },
                { title: '愤怒', url: 'fennu.gif' },
                { title: '感冒', url: 'ganmao.gif' },
                { title: '心', url: 'xin.gif' },
                { title: '伤心', url: 'shangxin.gif' },
                { title: '猪', url: 'zhu.gif' },
                { title: '熊猫', url: 'xiongmao.gif' },
                { title: '兔子', url: 'tuzi.gif' },
                { title: '喔克', url: 'ok.gif' },
                { title: '耶', url: 'ye.gif' },
                { title: '棒棒', url: 'good.gif' },
                { title: '不', url: 'no.gif' },
                { title: '赞', url: 'zan.gif' },
                { title: '来', url: 'lai.gif' },
                { title: '弱', url: 'ruo.gif' },
                { title: '草泥马', url: 'caonima.gif' },
                { title: '神马', url: 'shenma.gif' },
                { title: '囧', url: 'jiong.gif' },
                { title: '浮云', url: 'fuyun.gif' },
                { title: '给力', url: 'geili.gif' },
                { title: '围观', url: 'weiguan.gif' },
                { title: '威武', url: 'weiwu.gif' },
                { title: '话筒', url: 'huatong.gif' },
                { title: '蜡烛', url: 'lazhu.gif' },
                { title: '蛋糕', url: 'dangao.gif' },
                { title: '发红包', url: 'fahongbao.gif' }
            ]
        }
    },

    methods: {
        /**
         * 检查登录状态
         */
        checkLoginStatus() {
            var token = getToken()
            this.haslogin = !!token
        },

        /**
         * 未登录时跳转登录页
         */
        goLogin() {
            localStorage.setItem('logUrl', this.$route.fullPath)
            this.$router.push({ path: '/Login?login=1' })
        },

        /**
         * 获取评论头像
         */
        getAvatar(username) {
            if (username === '小胖') {
                return 'http://img04.sogoucdn.com/app/a/100520021/8d6526cd796fe882505096c10693c6db'
            }

            return this.$store.state.errorImg
        },

        setData(initData, result) {
            var msg = result.rows

            if (initData) {
                this.commentList = msg
            } else {
                this.commentList = this.commentList.concat(msg)
            }

            this.hasMore = result.total > this.commentList.length
        },

        choseEmoji(inner) {
            this.textarea += '[' + inner + ']'
        },

        analyzeEmoji(cont) {
            if (!cont) {
                return ''
            }

            var pattern1 = /[[\u4e00-\u9fa5]+]/g
            var pattern2 = /[[\u4e00-\u9fa5]+]/
            var content = cont.match(pattern1)
            var str = cont

            if (content) {
                for (var i = 0; i < content.length; i++) {
                    var src = ''

                    for (var j = 0; j < this.OwOlist.length; j++) {
                        if ('[' + this.OwOlist[j].title + ']' == content[i]) {
                            src = this.OwOlist[j].url
                            break
                        }
                    }

                    if (src) {
                        str = str.replace(pattern2, '<img src="static/img/emot/image/' + src + '"/>')
                    }
                }
            }

            return str
        },

        sendMsg() {
            var that = this

            that.checkLoginStatus()

            if (!that.haslogin) {
                that.goLogin()
                return
            }

            if (that.textarea) {
                that.sendTip = '咻~~'

                sendComment(
                    that.type,
                    that.aid,
                    that.rootId,
                    that.toCommentId,
                    that.toCommentUserId,
                    that.textarea
                ).then((response) => {
                    that.textarea = ''
                    that.rootId = -1
                    that.toCommentId = -1
                    that.toCommentUserId = -1

                    that.routeChange()
                    that.removeRespond()

                    var timer02 = setTimeout(function () {
                        that.sendTip = '发送~'
                        clearTimeout(timer02)
                    }, 1000)
                })
            } else {
                that.sendTip = '内容不能为空~'

                var timer = setTimeout(function () {
                    that.sendTip = '发送~'
                    clearTimeout(timer)
                }, 3000)
            }
        },

        respondMsg(rootId, toCommentId, toCommentUserId) {
            var that = this

            that.checkLoginStatus()

            if (that.haslogin) {
                var dom = event.currentTarget
                dom = dom.parentNode

                that.isRespond = true
                that.rootId = rootId
                that.toCommentId = toCommentId
                that.toCommentUserId = toCommentUserId

                if (that.$refs.respondBox) {
                    dom.appendChild(that.$refs.respondBox)
                }
            } else {
                that.$confirm('登录后才能回复评论，是否前往登录页面?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    that.goLogin()
                }).catch(() => {

                })
            }
        },

        removeRespond() {
            this.isRespond = false
            this.rootId = -1
            this.toCommentId = -1
            this.toCommentUserId = -1

            if (this.$refs.respondBox && this.$refs.tmsgBox && this.$refs.listDom) {
                this.$refs.tmsgBox.insertBefore(this.$refs.respondBox, this.$refs.listDom)
            }
        },

        showCommentList(initData) {
            var that = this

            if (that.$route.name == 'DetailArticle') {
                that.type = 0
                that.aid = parseInt(that.$route.query.aid)
                that.queryParams.articleId = that.aid

                getArticleComment(that.queryParams).then((response) => {
                    that.setData(initData, response)
                })
            } else if (that.$route.name == 'FriendsLink') {
                that.type = 1
                that.aid = null
                that.queryParams.articleId = null

                getLinkComment(that.queryParams).then((response) => {
                    that.setData(initData, response)
                })
            }
        },

        addMoreFun() {
            this.queryParams.pageNum++
            this.showCommentList(false)
        },

        routeChange() {
            this.queryParams.pageNum = 1
            this.checkLoginStatus()
            this.showCommentList(true)
        }
    },

    watch: {
        '$route': {
            handler() {
                this.routeChange()
            },
            immediate: false
        }
    },

    created() {
        this.routeChange()
    },

    activated() {
        this.routeChange()
    },

    mounted() {

    }
}
</script>

<style>
.tmsgBox {
    position: relative;
    background: #fff;
    padding: 15px;
    margin-bottom: 20px;
    border-radius: 5px;
}

.tmsg-respond h3 {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 8px;
}

.tmsg-respond h3 small {
    font-size: smaller;
    cursor: pointer;
}

.tmsg-respond textarea {
    background: #f4f6f7;
    height: 100px;
    margin-bottom: 10px;
}

.tmsg-login-tip {
    background: #f4f6f7;
    border: 1px dashed #dcdfe6;
    border-radius: 6px;
    padding: 24px 15px;
    text-align: center;
    color: #666;
    margin-bottom: 20px;
}

.tmsg-login-tip p {
    margin-bottom: 12px;
    font-size: 14px;
}

.OwO {
    position: relative;
    z-index: 1;
}

.OwO .OwO-logo {
    position: relative;
    border-radius: 4px;
    color: #444;
    display: inline-block;
    background: #fff;
    border: 1px solid #ddd;
    font-size: 13px;
    padding: 0 6px;
    cursor: pointer;
    height: 30px;
    box-sizing: border-box;
    z-index: 2;
    line-height: 30px;
}

.OwO .OwO-logo:hover {
    animation: a 5s infinite ease-in-out;
    -webkit-animation: a 5s infinite ease-in-out;
}

.OwO .OwO-body {
    position: absolute;
    background: #fff;
    border: 1px solid #ddd;
    z-index: 1;
    top: 29px;
    border-radius: 0 4px 4px 4px;
    display: none;
}

.OwO-open .OwO-body {
    display: block;
}

.OwO-open .OwO-logo {
    border-radius: 4px 4px 0 0;
    border-bottom: none;
}

.OwO-open .OwO-logo:hover {
    animation: none;
    -webkit-animation: none;
}

.OwO .OwO-items {
    max-height: 197px;
    overflow: scroll;
    font-size: 0;
    padding: 10px;
    z-index: 1;
}

.OwO .OwO-items .OwO-item {
    background: #f7f7f7;
    padding: 5px 10px;
    border-radius: 5px;
    display: inline-block;
    margin: 0 10px 12px 0;
    transition: 0.3s;
    line-height: 19px;
    font-size: 20px;
    cursor: pointer;
}

.OwO .OwO-items .OwO-item:hover {
    background: #eee;
    box-shadow:
        0 2px 2px 0 rgba(0, 0, 0, .14),
        0 3px 1px -2px rgba(0, 0, 0, .2),
        0 1px 5px 0 rgba(0, 0, 0, .12);
    animation: a 5s infinite ease-in-out;
    -webkit-animation: a 5s infinite ease-in-out;
}

.OwO .OwO-body .OwO-bar {
    width: 100%;
    height: 30px;
    border-top: 1px solid #ddd;
    background: #fff;
    border-radius: 0 0 4px 4px;
    color: #444;
}

.OwO .OwO-body .OwO-bar .OwO-packages li {
    display: inline-block;
    line-height: 30px;
    font-size: 14px;
    padding: 0 10px;
    cursor: pointer;
    margin-right: 3px;
    text-align: center;
}

.OwO .OwO-body .OwO-bar .OwO-packages li:first-of-type {
    border-radius: 0 0 0 3px;
}

@-webkit-keyframes a {
    0%,
    to {
        -webkit-transform: translate(0) rotate(0deg);
        transform: translate(0) rotate(0deg);
    }

    25% {
        -webkit-transform: translateY(1.5px) rotate(1.5deg);
        transform: translateY(1.5px) rotate(1.5deg);
    }

    50% {
        -webkit-transform: translateY(-1.5px) rotate(-.5deg);
        transform: translateY(-1.5px) rotate(-.5deg);
    }

    75% {
        -webkit-transform: translateY(.5px) rotate(.5deg);
        transform: translateY(.5px) rotate(.5deg);
    }
}

@keyframes a {
    0%,
    to {
        -webkit-transform: translate(0) rotate(0deg);
        transform: translate(0) rotate(0deg);
    }

    25% {
        -webkit-transform: translateY(1.5px) rotate(1.5deg);
        transform: translateY(1.5px) rotate(1.5deg);
    }

    50% {
        -webkit-transform: translateY(-1.5px) rotate(-.5deg);
        transform: translateY(-1.5px) rotate(-.5deg);
    }

    75% {
        -webkit-transform: translateY(.5px) rotate(.5deg);
        transform: translateY(.5px) rotate(.5deg);
    }
}

.tmsg-r-info {
    margin: 10px 0;
}

.tmsg-r-info input {
    height: 30px;
    border-radius: 4px;
    background: #f4f6f7;
}

.tmsg-r-info .info-submit {
    margin: 10px 0;
    text-align: center;
}

.tmsg-r-info .info-submit p,
.tmsg-commentshow h1 {
    color: #fff;
    border-radius: 5px;
    cursor: pointer;
    height: 30px;
    line-height: 30px;
    text-align: center;
}

.tmsg-comments .tmsg-comments-tip {
    display: block;
    border-left: 2px solid #363d4c;
    padding: 0 10px;
    margin: 40px 0;
    font-size: 20px;
}

.tmsg-commentlist {
    margin-bottom: 20px;
}

.tmsg-commentshow>.tmsg-commentlist {
    border-bottom: 1px solid #e5eaed;
}

.tmsg-c-item {
    border-top: 1px solid #e5eaed;
}

.tmsg-c-item article {
    margin: 20px 0;
}

.tmsg-c-item article header {
    margin-bottom: 10px;
}

.tmsg-c-item article header img {
    width: 65px;
    height: 65px;
    border-radius: 50%;
    float: left;
    transition: all .4s ease-in-out;
    -webkit-transition: all .4s ease-in-out;
    margin-right: 15px;
    object-fit: cover;
}

.tmsg-c-item article header img:hover {
    transform: rotate(360deg);
    -webkit-transform: rotate(360deg);
}

.tmsg-c-item article header .i-name {
    font-size: 14px;
    margin: 5px 8px 7px 0;
    color: #444;
    font-weight: bold;
    display: inline-block;
}

.tmsg-c-item article header .i-name span {
    color: #999;
    font-weight: normal;
    margin: 0 4px;
}

.tmsg-c-item article header .i-class {
    display: inline-block;
    margin-left: 10px;
    background: #dff0d8;
    color: #3c763d;
    border-radius: 5px;
    padding: 3px 6px;
    font-size: 12px;
    font-weight: 400;
}

.tmsg-c-item article header .i-time {
    color: #aaa;
    font-size: 12px;
}

.tmsg-c-item article section {
    margin-left: 80px;
}

.tmsg-c-item article section p img {
    vertical-align: middle;
}

.tmsg-c-item article section .tmsg-replay {
    margin: 10px 0;
    font-size: 12px;
    color: #64609E;
    cursor: pointer;
}
</style>