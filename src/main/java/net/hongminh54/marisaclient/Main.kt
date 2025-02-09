package net.hongminh54.marisaclient

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import java.net.URI
import java.awt.Desktop
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream
import java.io.File
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.InputStream
import javax.swing.JOptionPane
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.Call
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import javax.imageio.ImageIO
import java.awt.Taskbar
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.delay
import androidx.compose.ui.window.WindowPosition
import androidx.compose.foundation.layout.Box
import java.awt.image.BufferedImage
import androidx.compose.foundation.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.*
import androidx.compose.ui.input.pointer.pointerMoveFilter
import kotlin.system.exitProcess


// Hàm main: Khởi chạy ứng dụng Compose for Desktop
fun main() = application {
    var showSplash by remember { mutableStateOf(true) }
    val splashImage = remember { loadResourceImage("/splash.png") }
    val iconImage = remember { loadResourceImage("/icon.jpg") }

    iconImage?.let { setTaskbarIcon(it) }

    if (showSplash) {
        SplashScreen(splashImage) { showSplash = false }
    } else {
        MainWindow(iconImage?.toPainter()) { exitProcess(0) }
    }
}

// Splash Screen
@Composable
fun SplashScreen(image: BufferedImage?, onFinish: () -> Unit) {
    val scope = rememberCoroutineScope()

    Window(
        undecorated = true,
        transparent = true,
        resizable = false,
        state = rememberWindowState(size = DpSize(300.dp, 300.dp), position = WindowPosition(Alignment.Center)),
        onCloseRequest = onFinish
    ) {
        LaunchedEffect(Unit) {
            scope.launch {
                delay(3000)
                onFinish()
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (image != null) {
                Image(image.toPainter(), contentDescription = "Splash")
            } else {
                Text("Loading...", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Cửa sổ chính
@Composable
fun MainWindow(iconPainter: BitmapPainter?, onExit: () -> Unit) {
    Window(
        onCloseRequest = onExit,
        title = "MarisaClient Installer 1.0",
        state = rememberWindowState(size = DpSize(900.dp, 600.dp)),
        icon = iconPainter
    ) {
        AppUI(onExit)
    }
}

// Tải ảnh từ resource
fun loadResourceImage(path: String): BufferedImage? =
    runCatching { object {}.javaClass.getResourceAsStream(path)?.use { ImageIO.read(it) } }.getOrNull()

// Chuyển ảnh thành Painter
fun BufferedImage.toPainter() = BitmapPainter(this.toComposeImageBitmap())

// Đặt icon Taskbar (nếu hỗ trợ)
fun setTaskbarIcon(image: BufferedImage) {
    if (Taskbar.isTaskbarSupported()) {
        try {
            Taskbar.getTaskbar().iconImage = image
        } catch (_: UnsupportedOperationException) {
            println("⚠️ Taskbar không hỗ trợ icon.")
        }
    }
}

// UI chính với Sidebar có hiệu ứng Hover
@Composable
fun AppUI(onExit: () -> Unit) {
    var selectedTab by remember { mutableStateOf("install") }
    val minecraftFolder = remember { detectMinecraftFolder() } // ✅ Giữ nguyên logic

    MaterialTheme {
        Row(Modifier.fillMaxSize().background(Color(30, 30, 30))) {
            Sidebar(
                onExit = onExit,
                onShowUpdateLog = { selectedTab = "update_log" },
                onShowGuide = { selectedTab = "guide" }
            )
            MainPanel(selectedTab) { selectedTab = "install" }
        }
    }
}

// Sidebar chứa các nút Update Log, Hướng Dẫn, Thoát
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Sidebar(onExit: () -> Unit, onShowUpdateLog: () -> Unit, onShowGuide: () -> Unit) {
    var isHovered by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(250.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp)) // Góc bo tròn hơn
            .background(
                if (isHovered) Color(50, 50, 50).copy(alpha = 0.9f)
                else Color(40, 40, 40).copy(alpha = 0.8f)
            )
            .shadow(20.dp, shape = RoundedCornerShape(20.dp))
            .pointerMoveFilter(
                onEnter = { isHovered = true; false },
                onExit = { isHovered = false; false }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "MarisaClient Installer",
                color = Color(255, 223, 0),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
            SidebarButton("Update Log", Icons.Default.Info, onClick = onShowUpdateLog)
            SidebarButton("Hướng Dẫn", Icons.Default.Info, onClick = onShowGuide)
            SidebarButton("WebSite", Icons.Default.Call, onClick = { openWebPage("https://github.com/hongminh54/MarisaApplication") })
            Spacer(modifier = Modifier.weight(1f))
            SidebarButton("Thoát", Icons.Default.ExitToApp, onClick = onExit, baseColor = Color.Red)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SidebarButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    baseColor: Color = Color.White
) {
    var isHovered by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isHovered) baseColor.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .pointerMoveFilter(
                onEnter = { isHovered = true; false },
                onExit = { isHovered = false; false }
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = baseColor)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, color = baseColor, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AnimatedButton(
    text: String,
    baseColor: Color = Color.White,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isHovered) baseColor.copy(alpha = 0.3f) else baseColor.copy(alpha = 0.2f))
            .clickable { isPressed = true; onClick() }
            .pointerMoveFilter(
                onEnter = { isHovered = true; false },
                onExit = { isHovered = false; false }
            )
            .scale(if (isPressed) 0.95f else 1f) // Hiệu ứng thu nhỏ nhẹ khi nhấn
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// Panel chính hiển thị nội dung của từng tab
@Composable
fun MainPanel(selectedTab: String, onBackToInstall: () -> Unit) {
    var minecraftFolder by remember { mutableStateOf<File?>(null) }
    var status2 by remember { mutableStateOf("🔍 Đang kiểm tra thư mục...") }
    val isDownloading = remember { mutableStateOf(false) }
    val isPaused = remember { mutableStateOf(false) }
    var downloadJob by remember { mutableStateOf<Job?>(null) }
    val downloadScope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(0f) }
    var speed by remember { mutableStateOf("0 KB/s") }
    var status by remember { mutableStateOf("Sẵn sàng tải.") }
    val filePath = "./mods.zip"

    val statusColor by animateColorAsState(
        when {
            status.contains("Sẵn sàng") -> Color.LightGray
            status.contains("Đang tải") -> Color.Magenta
            status.contains("✅") -> Color.Green
            status.contains("❌") -> Color.Red
            else -> Color.Yellow
        }, label = "Status Color"
    )

    val logMessages = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        val detectedFolder = detectMinecraftFolder()
        minecraftFolder = detectedFolder
        status2 = if (detectedFolder != null) "📂 Đã tìm thấy: ${detectedFolder.absolutePath}"
        else "⚠️ Không tìm thấy thư mục .minecraft"
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        when (selectedTab) {
            "install" -> {
                Text("Trạng thái: $status", color = statusColor)

                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.DarkGray),
                    color = Color.Cyan
                )

                Text("Tốc độ: $speed", color = Color.White)
                Text("Thư mục Minecraft: $status2", color = Color.White, fontSize = 14.sp)

                AnimatedButton(text = "🔄 Update Folder", onClick = {
                    val detectedFolder = detectMinecraftFolder()
                    minecraftFolder = detectedFolder
                    status2 = if (detectedFolder != null) "📂 Đã tìm thấy: ${detectedFolder.absolutePath}"
                    else "⚠️ Không tìm thấy thư mục .minecraft"
                }, baseColor = Color.Green)

                if (minecraftFolder != null) {
                    AnimatedButton(text = "📂 Mở thư mục", onClick = {
                        openSelectedFolder(minecraftFolder!!)
                    }, baseColor = Color.Blue)
                }

                val listState = rememberLazyListState()
                LaunchedEffect(logMessages.size) {
                    listState.animateScrollToItem(logMessages.size)
                }

                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                        .background(Color(50, 50, 50)).padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                ) {
                    LazyColumn(state = listState) {
                        items(logMessages.size) { index ->
                            Text(logMessages[index], color = Color.White, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row {
                    AnimatedButton(text = "Bắt đầu tải", onClick = {
                        if (!isDownloading.value) {
                            isDownloading.value = true
                            isPaused.value = false
                            progress = 0f
                            speed = "0 KB/s"
                            status = "🔄 Đang tải..."
                            logMessages.clear()

                            downloadJob = downloadScope.launch {
                                downloadFile(
                                    url = "https://github.com/hongminh54/assets/releases/download/new/mods.zip", // Thay đổi URL tại đây
                                    outputFilePath = filePath,
                                    progress = { progress = it },
                                    speed = { speed = it },
                                    status = { status = it },
                                    log = { logMessages.add(it) },
                                    isPaused = { isPaused.value },
                                    onDownloadComplete = {
                                        isDownloading.value = false
                                        extractZip(File(filePath)) {
                                            status = "✅ Giải nén hoàn tất!"
                                            logMessages.add("✅ Giải nén hoàn tất!")
                                        }
                                    }
                                )
                            }
                        }
                    })

                    Spacer(Modifier.width(10.dp))

                    AnimatedButton(text = if (isPaused.value) "Tiếp tục" else "Tạm dừng", onClick = {
                        if (isDownloading.value) {
                            isPaused.value = !isPaused.value
                            status = if (isPaused.value) "⏸️ Đã tạm dừng" else "▶️ Tiếp tục tải..."
                            logMessages.add(status)
                        }
                    })

                    Spacer(Modifier.width(10.dp))

                    AnimatedButton(text = "Hủy", onClick = {
                        if (isDownloading.value) {
                            isDownloading.value = false
                            isPaused.value = false
                            progress = 0f
                            speed = "0 KB/s"
                            status = "❌ Tải bị hủy!"
                            logMessages.add("❌ Tải bị hủy!")
                            downloadJob?.cancel()
                            downloadJob = null
                        }
                    })
                }
            }

            "update_log" -> {
                Text("🔹 Update Log", color = Color.White, fontSize = 20.sp)
                Text("- Update 1.0 Pre-Release\n- Update Gui\n- Update MarisaClient to MarisaClient-1.0.2-2025\n- Improve Gui 1.1", color = Color.White)

                AnimatedButton(text = "Quay lại Cài Đặt", onClick = onBackToInstall)
            }

            "guide" -> {
                Text("📜 Hướng Dẫn Cài Đặt", color = Color.White, fontSize = 20.sp)
                Text(
                    "Thả thư mục mods vào thư mục .minecraft của bạn và chạy fabric mod phiên bản 1.21.4\n\n" +
                            "Nếu bạn muốn loại bỏ một số mod không cần thiết, bạn có thể loại bỏ chúng!\n" +
                            "Chúc bạn chơi vui vẻ\n", color = Color.White
                )

                AnimatedButton(text = "Quay lại Cài Đặt", onClick = onBackToInstall)
            }
        }
    }
}

// Mở Folder từ File .minecraft
fun openFolder(folder: File) {
    try {
        if (!folder.exists()) {
            JOptionPane.showMessageDialog(null, "⚠️ Thư mục không tồn tại: ${folder.absolutePath}", "Lỗi", JOptionPane.ERROR_MESSAGE)
            return
        }

        when {
            Desktop.isDesktopSupported() -> {
                Desktop.getDesktop().open(folder)
            }
            System.getProperty("os.name").contains("Linux", ignoreCase = true) -> {
                Runtime.getRuntime().exec("xdg-open ${folder.absolutePath}")
            }
            System.getProperty("os.name").contains("Mac OS", ignoreCase = true) -> {
                Runtime.getRuntime().exec("open ${folder.absolutePath}")
            }
            else -> JOptionPane.showMessageDialog(null, "⚠️ Không thể mở thư mục trên hệ điều hành này!", "Lỗi", JOptionPane.ERROR_MESSAGE)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// Tải xuống file từ mạng
suspend fun downloadFile(
    url: String,
    outputFilePath: String,
    progress: (Float) -> Unit,
    speed: (String) -> Unit,
    status: (String) -> Unit,
    log: (String) -> Unit,
    isPaused: () -> Boolean,
    onDownloadComplete: () -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            status("🔍 Đang kết nối đến máy chủ...")
            log("🔍 Đang kết nối máy chủ...")

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 20000
            connection.connect()

            val fileSize = connection.contentLength
            if (fileSize <= 0) {
                withContext(Dispatchers.Main) {
                    JOptionPane.showMessageDialog(null, "⚠️ Không thể lấy kích thước file!", "Lỗi", JOptionPane.ERROR_MESSAGE)
                }
                return@withContext
            }

            val inputStream: InputStream = connection.inputStream
            val outputFile = File(outputFilePath)
            val outputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(4096)
            var totalBytesRead = 0L
            var bytesRead: Int
            val startTime = System.currentTimeMillis()
            var lastLoggedPercent = -5
            var wasPaused = false  // ✅ Biến theo dõi trạng thái tạm dừng trước đó

            status("📥 Bắt đầu tải xuống...")
            log("📥 Đang tải xuống từ github")
            log("📂 Lưu file vào: ${outputFile.absolutePath}")

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                while (isPaused()) {
                    if (!wasPaused) {  // ✅ Chỉ log khi vừa chuyển trạng thái tạm dừng
                        status("⏸️ Đã tạm dừng")
                        log("⏸️ Quá trình tải bị tạm dừng")
                        wasPaused = true
                    }
                    delay(500)
                }

                wasPaused = false // Reset khi tiếp tục tải

                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                val percent = (totalBytesRead.toFloat() / fileSize) * 100
                val elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0
                val downloadSpeed = if (elapsedTime > 0) (totalBytesRead / elapsedTime / 1024).roundToInt() else 0

                val downloadedMB = totalBytesRead / (1024 * 1024)
                val totalMB = fileSize / (1024 * 1024)

                withContext(Dispatchers.Main) {
                    progress(percent / 100)
                    speed("$downloadSpeed KB/s")
                    status("📥 Đang tải... ($downloadedMB/$totalMB MB) • ${percent.roundToInt()}%")

                    if (percent.roundToInt() >= lastLoggedPercent + 5) {
                        log("📥 Đang tải... ($downloadedMB/$totalMB MB) • ${percent.roundToInt()}%")
                        lastLoggedPercent = percent.roundToInt()
                    }
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            withContext(Dispatchers.Main) {
                status("✅ Tải hoàn tất!")
                log("✅ Tải xong file $outputFilePath")
                onDownloadComplete()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                JOptionPane.showMessageDialog(null, "⚠️ Lỗi khi tải file: ${e.message}", "Lỗi", JOptionPane.ERROR_MESSAGE)
                status("❌ Lỗi tải file!")
                log("❌ Lỗi khi tải file: ${e.message}")
            }
        }
    }
}

// Phát hiện thư mục .minecraft
fun detectMinecraftFolder(): File? {
    val home = System.getProperty("user.home")
    val possiblePaths = listOf(
        "$home/.minecraft", // Linux & MacOS
        "$home/AppData/Roaming/.minecraft" // Windows
    )

    for (path in possiblePaths) {
        val folder = File(path)
        if (folder.exists()) return folder
    }
    return null
}

// Thông báo mở file sau khi giải nén
fun openSelectedFolder(folder: File) {
    try {
        // Kiểm tra xem thư mục có tồn tại không
        if (!folder.exists()) {
            // Hiển thị thông báo lỗi nếu thư mục không tồn tại
            JOptionPane.showMessageDialog(
                null,
                "⚠️ Thư mục không tồn tại: ${folder.absolutePath}",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
            )
            return // Thoát khỏi hàm nếu thư mục không tồn tại
        }

        // Kiểm tra hệ điều hành và mở thư mục tương ứng
        when {
            Desktop.isDesktopSupported() -> {
                // Nếu hệ thống hỗ trợ Desktop API, sử dụng để mở thư mục
                Desktop.getDesktop().open(folder)
            }
            System.getProperty("os.name").contains("Linux", ignoreCase = true) -> {
                // Nếu là hệ điều hành Linux, sử dụng lệnh `xdg-open` để mở thư mục
                Runtime.getRuntime().exec("xdg-open ${folder.absolutePath}")
            }
            System.getProperty("os.name").contains("Mac OS", ignoreCase = true) -> {
                // Nếu là hệ điều hành macOS, sử dụng lệnh `open` để mở thư mục
                Runtime.getRuntime().exec("open ${folder.absolutePath}")
            }
            else -> {
                // Nếu không hỗ trợ mở thư mục trên hệ điều hành này, hiển thị thông báo lỗi
                JOptionPane.showMessageDialog(
                    null,
                    "⚠️ Không thể mở thư mục trên hệ điều hành này!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
                )
            }
        }
    } catch (e: Exception) {
        // Bắt lỗi nếu có lỗi xảy ra và in ra log
        e.printStackTrace()
    }
}

// Tự giải nén file và xóa file zip sau khi tải xong
fun extractZip(zipFile: File, onExtractComplete: (File) -> Unit) {
    // Xác định thư mục đích để giải nén, đặt cùng vị trí với file zip và có tên giống file zip
    val outputDir = File(zipFile.parentFile, zipFile.nameWithoutExtension)

    // Nếu thư mục đích chưa tồn tại, tạo thư mục mới
    if (!outputDir.exists()) outputDir.mkdirs()

    // Mở file zip dưới dạng stream
    ZipInputStream(zipFile.inputStream()).use { zis ->
        var entry = zis.nextEntry // Lấy mục (entry) đầu tiên trong file zip

        // Lặp qua từng mục trong file zip
        while (entry != null) {
            val newFile = File(outputDir, entry.name) // Tạo đường dẫn file tương ứng trong thư mục giải nén

            if (entry.isDirectory) {
                // Nếu là thư mục, tạo thư mục mới
                newFile.mkdirs()
            } else {
                // Nếu là file, tạo thư mục cha (nếu chưa có) và ghi dữ liệu từ file zip vào
                newFile.parentFile.mkdirs()
                newFile.outputStream().use { zis.copyTo(it) } // Sao chép dữ liệu từ zip vào file mới
            }
            entry = zis.nextEntry // Chuyển sang mục tiếp theo trong file zip
        }
    }
    // Xóa file zip sau khi giải nén hoàn tất
    zipFile.delete()

    // Gọi callback khi giải nén hoàn tất
    onExtractComplete(outputDir)

    // Hiển thị thông báo sau khi giải nén xong
    val result = JOptionPane.showConfirmDialog(
        null,
        "✅ Giải nén hoàn tất!\nBạn có muốn mở thư mục không?",
        "Thông báo",
        JOptionPane.YES_NO_OPTION
    )

    // Mở thư mục vừa giải nén
    if (result == JOptionPane.YES_OPTION) {
        openFolder(outputDir)
    }
}

/**
* Mở trang web trong trình duyệt mặc định.
*
* @param url Địa chỉ trang web cần mở.
*/
fun openWebPage(url: String) {
    // Kiểm tra nếu URL rỗng hoặc không hợp lệ
    if (url.isBlank()) {
        JOptionPane.showMessageDialog(null, "⚠️ URL không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE)
        return
    }

    try {
        val uri = URI(url) // Chuyển đổi chuỗi URL thành đối tượng URI

        // Kiểm tra xem hệ thống có hỗ trợ Desktop API không
        if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(uri) // Mở trang web bằng trình duyệt mặc định
                return
            }
        }

        // Nếu Desktop API không khả dụng, dùng lệnh hệ thống
        val osName = System.getProperty("os.name").lowercase()
        val command = when {
            osName.contains("win") -> "rundll32 url.dll,FileProtocolHandler $url" // Windows
            osName.contains("mac") -> "open $url" // macOS
            osName.contains("nux") -> "xdg-open $url" // Linux
            else -> null
        }

        if (command != null) {
            Runtime.getRuntime().exec(command) // Thực thi lệnh mở trình duyệt
        } else {
            JOptionPane.showMessageDialog(null, "⚠️ Không thể mở trình duyệt trên hệ điều hành này!", "Lỗi", JOptionPane.ERROR_MESSAGE)
        }

    } catch (e: Exception) {
        JOptionPane.showMessageDialog(null, "⚠️ Không thể mở trang web: ${e.message}", "Lỗi", JOptionPane.ERROR_MESSAGE)
    }
}