package com.example.julive.wechathelper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Notification
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast


class WechatService : AccessibilityService() {

    private val handler = Handler()

    override fun onInterrupt() {
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkgName = event?.packageName.toString()
        val eventType = event?.eventType
        val className = event?.className
        val config = FileUtil.readLogByString(logPath, "0")
        log("\nconfig $config")
        log("className==\"$className\"")
        when (eventType) {
            AccessibilityEvent.TYPE_WINDOWS_CHANGED -> {
            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                when (config) {
                    "1" -> {
                        autoAddFriend(className)
                    }
                    "2" -> {
                        log("test");
                        DFS(rootInActiveWindow);
                        autoSendImage(className)
                    }
                    "3" -> {
//                        autoShareMiniPrograms(className)
                    }
                    "0" -> {

                    }
                }
            }
        }
    }

    private fun autoShareMiniPrograms(className: CharSequence?) {
        if (className == "com.tencent.mm.ui.LauncherUI") {
            click("??????")
            handler.postDelayed({
                click("?????????")
            }, 1000)
        }
        if (className == "com.tencent.mm.plugin.appbrand.ui.AppBrandLauncherUI") {
            if (isOneTime)
                performMenuDoubleClick {
                    click("??????")
                    handler.postDelayed({
                        input("????????????")

                    }, 1000)
                }
        }
    }

    private fun autoSendImage(className: CharSequence?) {
        log("??????????????? autoSendImage ==\"$className\"");

        if (className == "com.tencent.mm.ui.LauncherUI") {
            click("??????")
            handler.postDelayed({
                click("?????????")
            }, 1000)
        }
        if (className == "com.tencent.mm.plugin.sns.ui.SnsTimeLineUI") {
            click("????????????")
            click("???????????????")
        }
        if (className == "com.tencent.mm.plugin.gallery.ui.AlbumPreviewUI") {
            if (isOneTime)
                performMenuDoubleClick {
                    choosePicture(0, 1)

                    handler.postDelayed({
                        clickById("com.tencent.mm:id/d6")
                    }, 1000)
                }
//                choosePicture(0, 1)
//                click("??????(1/9)")


        }
        if (className == "com.tencent.mm.plugin.sns.ui.SnsUploadUI") {


//            handler.postDelayed({
//                copyText("com.tencent.mm:id/hxn",
//                    "????????????????????????\r\n\r\n" +
//                            "??????????????????????????????\r\n" +
//                            "??????????????????\r\n" +
//                            "????????????\r\n" +
//                            "????????????\r\n"+
//                            "?????????????????????\r\n")
//                handler.postDelayed({
//                    clickById("com.tencent.mm:id/d6")
//                }, 1000)
//            }, 1000)
            isOneTime = true
            resetConfig()
        }
    }

    private fun autoAddFriend(className: CharSequence?) {
        if (className == "com.tencent.mm.ui.LauncherUI") {
            click("??????")
            click("??????")
            click("????????????")
        }
        if (className == "com.tencent.mm.plugin.subapp.ui.pluginapp.AddMoreFriendsUI") {
            click("?????????")
        }
        if (className == "com.tencent.mm.plugin.fts.ui.FTSAddWw") {
            handler.postDelayed({
                input("13261103711")
                handler.postDelayed({
                    clickById("com.tencent.mm:id/px")
                }, 1000)
            }, 1000)
        }
        if (className == "com.tencent.mm.plugin.profile.ui.ContactInfoUI") {
            click("??????????????????")
        }
        if (className == "com.tencent.mm.plugin.profile.ui.SayHiWithSnsPermissionUI") {
            Toast.makeText(this, "????????????????????????????????????????????????", Toast.LENGTH_SHORT).show()
            resetConfig()
        }
    }

    /**
     * ????????????
     */
    private fun resetConfig() {
        FileUtil.writeLog(logPath, "0", false, "utf-8")
        stopSelf() //?????????????????? ?????????????????????????????????
    }

    /**
     * ????????????????????????????????????
     */
    private fun DFS(rootInfo: AccessibilityNodeInfo?) {
        if (rootInfo == null || TextUtils.isEmpty(rootInfo.className)) {
            return
        }
        if (rootInfo.childCount >0) {
            log("DFS ???????????????:$rootInfo")

            for (i in 0 until rootInfo.childCount) {
                DFS(rootInfo.getChild(i))
            }
        } else {
            log("DFS ??????????????????")
            val text = rootInfo.text;
            val name =rootInfo.className.toString();
            val viewIdResourceName =rootInfo.viewIdResourceName;
            log("DFS ?????????????????????$rootInfo")
            log("DFS  ????????????=$text[$name][$viewIdResourceName]")
        }
    }
    /**
     * ???????????????nodeInfo
     * @param str text?????????
     */
    private fun click(str: String, action: Int = AccessibilityNodeInfo.ACTION_CLICK) {

        handler.postDelayed({
            val nodeInfo = rootInActiveWindow

            if (nodeInfo == null) {
                Toast.makeText(this, "rootWindow??????", Toast.LENGTH_SHORT).show()
                return@postDelayed
            }

            val list = nodeInfo.findAccessibilityNodeInfosByText(str)
            log("???????????????????????????$list")
            if (list != null && list.size > 0) {
                list[list.size - 1].performAction(action)
                list[list.size - 1].parent?.performAction(action)
            } else {
                Toast.makeText(this, "click ????????????????????????[$str]", Toast.LENGTH_SHORT).show()
            }
            nodeInfo.recycle()
        }, 1000)
    }

    /**
     * ???????????????nodeInfo
     * @param str text?????????
     */
    private fun clickById(str: String, action: Int = AccessibilityNodeInfo.ACTION_CLICK) {
        handler.postDelayed({
            val nodeInfo = rootInActiveWindow
            if (nodeInfo == null) {
                Toast.makeText(this, "rootWindow??????", Toast.LENGTH_SHORT).show()
                return@postDelayed
            }
            val list = nodeInfo.findAccessibilityNodeInfosByViewId(str)
            log(list.toString())
            if (list != null && list.size > 0) {
                list[list.size - 1].performAction(action)
                list[list.size - 1].parent?.performAction(action)
            } else {
                Toast.makeText(this, "clickById ????????????????????????", Toast.LENGTH_SHORT).show()
            }
            nodeInfo.recycle()
        }, 1000)
    }

    //???????????????????????????
    private fun input(hello: String) {
        handler.postDelayed({
            val nodeInfo = rootInActiveWindow
            if (nodeInfo == null) {
                Toast.makeText(this, "rootWindow??????", Toast.LENGTH_SHORT).show()
                return@postDelayed
            }
            //???????????????????????????view
            val target = nodeInfo.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

            if (target == null) {
                log("input: null")
                return@postDelayed
            }
            val arguments = Bundle()
            arguments.putCharSequence(
                AccessibilityNodeInfo
                    .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, hello
            )
            target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            nodeInfo.recycle()
        }, 1000)
    }
    private  fun copyText(id:String,text :String){
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", text);
        clipboard.primaryClip = clip;

        val nodeInfo = rootInActiveWindow
        val list = nodeInfo.findAccessibilityNodeInfosByViewId(id)
        log(list.toString())
        if (list != null && list.size > 0) {
            //?????????n???AccessibilityNodeInfo?????????
            val node = list[list.size - 1];
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            //        ??????????????????
            node.performAction(AccessibilityNodeInfo.ACTION_PASTE);

        } else {
            Toast.makeText(this, "clickById ????????????????????????", Toast.LENGTH_SHORT).show()
        }
        nodeInfo.recycle()

    }
    private fun log(config: String?) {
        Log.d("AccessibilityNodeInfo", config)
    }

    /**
     * ?????????????????????
     */
    private fun openNotification(event: AccessibilityEvent) {
        if (event.parcelableData == null || event.parcelableData !is Notification) {
            return
        }
        //????????????????????????
        val notification = event.parcelableData as Notification
        val pendingIntent = notification.contentIntent
        try {
            pendingIntent.send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }

    /**
     * ??????????????????
     */
    private fun performBackClick() {
        handler.postDelayed({ performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }, 1300L)
    }

    /**
     * ?????????
     */
    private fun performHomeClick() {
        handler.postDelayed({
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        }, 1300L)
    }

    /**
     * ??????????????????
     */
    private fun performMenuClick() {
        handler.postDelayed({
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
        }, 1300L)
    }

    /**
     * ??????????????????????????????
     */
    private var isOneTime = true

    /**
     * ????????????????????????????????????????????????
     * ?????????????????????????????????????????????????????????
     */
    private fun performMenuDoubleClick(doubleCallBack: () -> Unit) {
        performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
        handler.postDelayed({
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            doubleCallBack()
            isOneTime = false
        }, 1000)
    }

    /**
     * ???????????????
     */
    private fun performClickBtn(accessibilityNodeInfoList: List<AccessibilityNodeInfo>?): Boolean {
        if (accessibilityNodeInfoList != null && accessibilityNodeInfoList.isNotEmpty()) {
            for (i in accessibilityNodeInfoList.indices) {
                val accessibilityNodeInfo = accessibilityNodeInfoList[i]
                if (accessibilityNodeInfo.isClickable && accessibilityNodeInfo.isEnabled) {
                    accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
            }
        }
        return false
    }

    /**
     * ????????????
     *
     * @param startPicIndex ??????startPicIndex????????????
     * @param picCount      ?????????picCount???
     */
    private fun choosePicture(startPicIndex: Int, picCount: Int) {
        handler.postDelayed({
            val accessibilityNodeInfo = rootInActiveWindow
            if (accessibilityNodeInfo == null) {
                Toast.makeText(this, "accessibilityNodeInfo is null", Toast.LENGTH_SHORT).show()
                return@postDelayed
            }
            val accessibilityNodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByText("??????")
            if (accessibilityNodeInfoList == null ||
                accessibilityNodeInfoList.size == 0 ||
                accessibilityNodeInfoList[0].parent == null ||
                accessibilityNodeInfoList[0].parent.childCount == 0
            ) {
                return@postDelayed
            }
            val tempInfo = accessibilityNodeInfoList[0].parent.getChild(3)

            for (j in startPicIndex until startPicIndex + picCount) {
                val childNodeInfo = tempInfo.getChild(j)
                if (childNodeInfo != null) {
                    for (k in 0 until childNodeInfo.childCount) {
                        if (childNodeInfo.getChild(k).isEnabled && childNodeInfo.getChild(k).isClickable) {
                            childNodeInfo.getChild(k).performAction(AccessibilityNodeInfo.ACTION_CLICK)//????????????
                        }
                    }
                }
            }
            val finishList = accessibilityNodeInfo.findAccessibilityNodeInfosByText("??????($picCount/9)")//????????????
            performClickBtn(finishList)
        }, 2000)
    }


    /**
     * ????????????
     * ???????????? 0~20
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun slideVertical(startSlideRatio: Int, stopSlideRatio: Int) {
        log("slideVertical")
        val screenHeight = getScreenHeight(this)
        val screenWidth = getScreenWidth(this)
        log("screenHeight $screenHeight")
        log("screenWidth $screenWidth")
        val path = Path()
        val start = screenHeight / 20 * startSlideRatio
        val stop = screenHeight / 20 * stopSlideRatio
        path.moveTo((screenWidth / 2).toFloat(), start.toFloat())//??????????????????moveTo????????????
        path.lineTo((screenWidth / 2).toFloat(), stop.toFloat())//??????????????????????????????
        val builder = GestureDescription.Builder()
        val gestureDescription = builder
            .addStroke(
                GestureDescription.StrokeDescription(
                    path,
                    0,
                    500
                )
            )
            .build()

        dispatchGesture(gestureDescription, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                super.onCompleted(gestureDescription)
                log("onCompleted")
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                super.onCancelled(gestureDescription)
                log("onCancelled")
            }
        }, null)
    }

    private fun getScreenWidth(context: Context): Int {
        val wm = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
    }

    private fun getScreenHeight(context: Context): Int {
        val wm = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val outMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(outMetrics)
        return outMetrics.heightPixels
    }
}
