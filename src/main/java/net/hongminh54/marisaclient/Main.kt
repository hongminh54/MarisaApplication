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
import androidx.compose.ui.window.*
import kotlinx.coroutines.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.*
import kotlin.math.sin
import androidx.compose.ui.graphics.drawscope.Fill
import kotlin.math.PI
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.Call
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput

// Hàm main: Khởi chạy ứng dụng Compose for Desktop
fun main() = application {
    val scope = rememberCoroutineScope()
    Window(
        onCloseRequest = { scope.launch { exitApplication() } }, // Đóng ứng dụng khi nhấn nút X
        title = "MarisaClient Installer 1.0",
        state = rememberWindowState(width = 900.dp, height = 600.dp) // Kích thước mặc định của cửa sổ
    ) {
        AppUI(onExit = { scope.launch { exitApplication() } }) // Gọi giao diện chính
    }
}

// Hàm hiển thị giao diện chính của ứng dụng
@Composable
fun AppUI(onExit: () -> Unit) {
    var selectedTab by remember { mutableStateOf("install") } // Biến trạng thái xác định tab đang được chọn
    val minecraftFolder = remember { detectMinecraftFolder() } // Tự động phát hiện thư mục .minecraft

    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize().background(Color(30, 30, 30))) {
            Sidebar(
                // Sidebar chứa các nút điều hướng
                onExit = onExit,
                onShowUpdateLog = { selectedTab = "update_log" },
                onShowGuide = { selectedTab = "guide" }
            )
            // Panel chính hiển thị nội dung theo tab
            MainPanel(selectedTab, minecraftFolder) { selectedTab = "install" }
        }
    }
}

// Sidebar chứa các nút Update Log, Hướng Dẫn, Thoát
@Composable
fun Sidebar(onExit: () -> Unit, onShowUpdateLog: () -> Unit, onShowGuide: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = Modifier
            .width(250.dp)
            .fillMaxHeight()
            .background(Color(40, 40, 40))
            .padding(16.dp)
            .drawBehind {
                val waveAmplitude = 20f
                val waveFrequency = 0.02f
                val path = Path().apply {
                    moveTo(0f, size.height * 0.8f)
                    for (x in 0..size.width.toInt()) {
                        val y = size.height * 0.8f + waveAmplitude * sin(waveFrequency * x + waveOffset * 2 * PI.toFloat())
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Red.copy(alpha = 0.5f),
                            Color.Green.copy(alpha = 0.5f),
                            Color.Blue.copy(alpha = 0.5f)
                        ),
                        startY = 0f,
                        endY = size.height
                    ),
                    style = Fill
                )
            },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "MarisaClient Installer",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        SidebarButton("Update Log", Icons.Default.Info, onClick = onShowUpdateLog)
        SidebarButton("Hướng Dẫn", Icons.Default.Info, onClick = onShowGuide)
        SidebarButton("WebSite", Icons.Default.Call, onClick = { openWebPage("https://www.facebook.com/elliotvatybzi/") })
        Spacer(modifier = Modifier.weight(1f))
        SidebarButton("Thoát", Icons.Default.ExitToApp, onClick = onExit, baseColor = Color.Red)
    }
}

@Composable
fun SidebarButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    baseColor: Color = Color.DarkGray,
    isSelected: Boolean = false
) {
    var hover by remember { mutableStateOf(false) }
    val neonGradient = listOf(Color.Cyan, Color.Magenta, Color.Blue)

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = baseColor,
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(50.dp)
            .border(
                width = if (hover) 2.dp else 1.dp,
                brush = Brush.linearGradient(neonGradient),
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text, fontSize = 16.sp)
        }
    }
}



// Panel chính hiển thị nội dung của từng tab
@Composable
fun MainPanel(selectedTab: String, initialMinecraftFolder: File?, onBackToInstall: () -> Unit) {
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
            status.contains("Sẵn sàng") -> Color.Gray
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

                // Cải thiện thanh tiến trình
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

                // Nút cập nhật để kiểm tra lại thư mục
                NeonButton(text = "🔄 Update Folder", onClick = {
                    val detectedFolder = detectMinecraftFolder()
                    minecraftFolder = detectedFolder
                    status2 = if (detectedFolder != null) "📂 Đã tìm thấy: ${detectedFolder.absolutePath}"
                    else "⚠️ Không tìm thấy thư mục .minecraft"
                }, baseColor = Color.Green)

                // Hiển thị nút mở thư mục nếu đã tìm thấy `.minecraft`
                if (minecraftFolder != null) {
                    NeonButton(text = "📂 Mở thư mục", onClick = {
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
                    // Nút "Bắt đầu tải" với hiệu ứng neon động
                    NeonButton(text = "Bắt đầu tải", onClick = {
                        if (!isDownloading.value) {
                            isDownloading.value = true
                            isPaused.value = false
                            progress = 0f
                            speed = "0 KB/s"
                            status = "🔄 Đang tải..."
                            logMessages.clear()

                            downloadJob = downloadScope.launch {
                                downloadFile(
                                    url = "https://github.com/hongminh54/assets/releases/download/new/mods.zip",
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

                    // Nút "Tạm dừng" với hiệu ứng neon động
                    NeonButton(text = if (isPaused.value) "Tiếp tục" else "Tạm dừng", onClick = {
                        if (isDownloading.value) {
                            isPaused.value = !isPaused.value
                            status = if (isPaused.value) "⏸️ Đã tạm dừng" else "▶️ Tiếp tục tải..."
                            logMessages.add(status)
                        }
                    })

                    Spacer(Modifier.width(10.dp))

                    // Nút "Hủy" với hiệu ứng neon động
                    NeonButton(text = "Hủy", onClick = {
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
                Text("- Update 1.0 Pre-Release\n- Update Gui\n- Update MarisaClient to MarisaClient-1.0.2-2025", color = Color.White)

                // Dùng NeonButton thay vì Button
                NeonButton(text = "Quay lại Cài Đặt", onClick = onBackToInstall)
            }

            "guide" -> {
                Text("📜 Hướng Dẫn Cài Đặt", color = Color.White, fontSize = 20.sp)
                Text(
                    "Thả thư mục mods vào thư mục .minecraft của bạn và chạy fabric mod phiên bản 1.21.4\n\n" +
                            "Nếu bạn muốn loại bỏ một số mod không cần thiết, bạn có thể loại bỏ chúng!\n\n" +
                            "Chúc bạn chơi vui vẻ\n", color = Color.White
                )

                // Dùng NeonButton thay vì Button
                NeonButton(text = "Quay lại Cài Đặt", onClick = onBackToInstall)
            }
        }
    }
}

// 🌟 Nút với hiệu ứng neon động
@Composable
fun NeonButton(text: String, onClick: () -> Unit, baseColor: Color = Color.DarkGray) {
    var hover by remember { mutableStateOf(false) }
    val neonGradient = listOf(Color.Cyan, Color.Magenta, Color.Blue)

    Box(
        modifier = Modifier
            .width(220.dp)
            .height(50.dp)
            .padding(8.dp)
            .border(
                width = if (hover) 4.dp else 2.dp,
                brush = Brush.linearGradient(neonGradient),
                shape = RoundedCornerShape(12.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { hover = true },
                    onTap = { hover = false }
                )
            }
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = baseColor,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

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

                wasPaused = false // ✅ Reset khi tiếp tục tải

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

// Mở thư mục được chọn
fun openMinecraftFolder() {
    val minecraftFolder = detectMinecraftFolder()

    if (minecraftFolder != null) {
        openSelectedFolder(minecraftFolder)
    } else {
        JOptionPane.showMessageDialog(null, "⚠️ Không tìm thấy thư mục .minecraft!", "Lỗi", JOptionPane.ERROR_MESSAGE)
    }
}

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

// Hàm mở trang web trong trình duyệt mặc định
fun openWebPage(url: String) {
    try {
        val uri = java.net.URI(url)
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(uri)
        } else {
            Runtime.getRuntime().exec("xdg-open $url") // Linux
        }
    } catch (e: Exception) {
        JOptionPane.showMessageDialog(null, "⚠️ Không thể mở trang web!", "Lỗi", JOptionPane.ERROR_MESSAGE)
    }
}