package com.example.bishun.ui.character

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

data class HelpSectionText(
    val title: String,
    val description: String,
    val bullets: List<String>,
)

data class PolicySectionText(
    val title: String,
    val bullets: List<String>,
)

data class SummaryRowText(val title: String, val detail: String)

data class LocalizedStrings(
    val locale: Locale,
    val appTitle: String,
    val searchLabel: String,
    val loadButton: String,
    val clearButton: String,
    val loadingLabel: String,
    val coursesDialogTitle: String,
    val coursePlanHeading: String,
    val courseIntroTitle: String,
    val courseIntroBullets: List<String>,
    val courseEmptyTitle: String,
    val courseEmptyDescription: String,
    val courseNoDataMessage: String,
    val expandCharactersLabel: String,
    val collapseCharactersLabel: String,
    val legendTitle: String,
    val legendGesture: String,
    val legendActiveLabel: String,
    val legendCompletedLabel: String,
    val legendRemainingLabel: String,
    val courseLegendHint: String,
    val courseLevelCompleteLabel: String,
    val courseLevelStartFormat: String,
    val courseLevelNextFormat: String,
    val levelLabelFormat: String,
    val courseLevelProgressFormat: String,
    val loadLevelFormat: String,
    val filterAllLabel: String,
    val filterRemainingLabel: String,
    val filterCompletedLabel: String,
    val helpTitle: String,
    val helpSections: List<HelpSectionText>,
    val helpConfirm: String,
    val privacyTitle: String,
    val privacyIntro: String,
    val dataSafetyHeading: String,
    val privacySummaryRows: List<SummaryRowText>,
    val contactSupportLabel: String,
    val emailSupportButton: String,
    val viewPolicyButton: String,
    val fullPolicyTitle: String,
    val fullPolicySections: List<PolicySectionText>,
)

@Composable
fun rememberLocalizedStrings(languageOverride: String?): LocalizedStrings {
    val contextLocale = LocalContext.current.resources.configuration.locales[0]
    val targetLocale = remember(languageOverride, contextLocale) {
        languageOverride?.takeIf { it.isNotBlank() }?.let { Locale.forLanguageTag(it) } ?: contextLocale
    }
    return remember(targetLocale) {
        when (targetLocale.language) {
            "es" -> localizedStringsEs(targetLocale)
            "ja" -> localizedStringsJa(targetLocale)
            else -> localizedStringsEn(targetLocale)
        }
    }
}

private fun localizedStringsEn(locale: Locale): LocalizedStrings = LocalizedStrings(
    locale = locale,
    appTitle = "Hanzi Stroke Order",
    searchLabel = "Hanzi",
    loadButton = "Load character",
    clearButton = "Clear input",
    loadingLabel = "Loading...",
    coursesDialogTitle = "HSK Courses",
    coursePlanHeading = "Pick a course to continue",
    courseIntroTitle = "How courses work",
    courseIntroBullets = listOf(
        "Choose an HSK level to begin or jump back in.",
        "Tap a character to practice; long-press to mark it mastered.",
        "The badge above the board mirrors your current course progress.",
    ),
    courseEmptyTitle = "No active course yet",
    courseEmptyDescription = "Select an HSK level below to start a guided session.",
    courseNoDataMessage = "No course data found. Please double-check the resource pack.",
    expandCharactersLabel = "Show all",
    collapseCharactersLabel = "Hide",
    legendTitle = "Legend & gestures",
    legendGesture = "Tap any character to practice; long-press to mark it as learned.",
    legendActiveLabel = "Active",
    legendCompletedLabel = "Completed",
    legendRemainingLabel = "Remaining",
    courseLegendHint = "Badge controls let you resume, skip, restart, or exit without covering the board.",
    courseLevelCompleteLabel = "Course complete",
    courseLevelStartFormat = "Start with %s",
    courseLevelNextFormat = "Next • %s",
    levelLabelFormat = "Level %d",
    courseLevelProgressFormat = "%1\$d/%2\$d mastered · %3\$d remaining",
    loadLevelFormat = "Load HSK %d",
    filterAllLabel = "All",
    filterRemainingLabel = "Remaining",
    filterCompletedLabel = "Completed",
    helpTitle = "Help & onboarding",
    helpSections = listOf(
        HelpSectionText(
            title = "Quick start",
            description = "Complete a full demo → practice cycle in seconds.",
            bullets = listOf(
                "Enter any hanzi and tap the cloud icon to load offline data.",
                "Use Play or Loop to watch the demo; enable the calligraphy template for tracing.",
                "Press the pencil on the board to begin; the hint icon highlights the next stroke.",
            ),
        ),
        HelpSectionText(
            title = "Courses & progress",
            description = "HSK courses live under the avatar menu, and the badge reflects your status.",
            bullets = listOf(
                "Resume/Skip/Restart/Exit icons stay inside the badge so controls never hide the board.",
                "Chevron arrows jump between characters; finishing one updates the summary instantly.",
            ),
        ),
        HelpSectionText(
            title = "Cards & pronunciation",
            description = "The Kaishu card shows dictionary data plus TextToSpeech playback.",
            bullets = listOf(
                "Tap the card to open the full definition and scroll through the explanation.",
                "The speaker button plays Mandarin audio entirely offline.",
            ),
        ),
        HelpSectionText(
            title = "Grids & templates",
            description = "Board settings support Mi-grid, nine-grid, or no guides at all.",
            bullets = listOf(
                "Switch between outline and freestyle modes; stroke colors persist between sessions.",
                "Scroll outside the canvas to move the page so the writing surface stays fixed.",
            ),
        ),
        HelpSectionText(
            title = "Support & feedback",
            description = "Need help? Reach out anytime.",
            bullets = listOf(
                "Email: qq260316514@gmail.com",
                "Profile → Feedback lets you attach logs before sending.",
            ),
        ),
    ),
    helpConfirm = "Got it",
    privacyTitle = "Privacy preferences",
    privacyIntro = "Everything runs offline by default. Enable diagnostics only if you agree.",
    dataSafetyHeading = "Data safety snapshot",
    privacySummaryRows = listOf(
        SummaryRowText(
            title = "Data storage",
            detail = "Practice history, course progress, and board settings stay in local DataStore only.",
        ),
        SummaryRowText(
            title = "Offline assets",
            detail = "Stroke JSON and course packs ship inside the APK; optional prefetch won't run unless enabled.",
        ),
        SummaryRowText(
            title = "Logs & feedback",
            detail = "Text logs are attached only when you explicitly send feedback.",
        ),
    ),
    contactSupportLabel = "Contact: %s",
    emailSupportButton = "Email support",
    viewPolicyButton = "View full policy",
    fullPolicyTitle = "Privacy policy",
    fullPolicySections = listOf(
        PolicySectionText(
            title = "1. Data we collect",
            bullets = listOf(
                "Character resources packaged in the APK for rendering stroke order.",
                "Up to 40 local practice records for resuming sessions.",
                "Board preferences such as grids, colors, and template visibility.",
                "Optional plain-text logs attached only when you send feedback.",
            ),
        ),
        PolicySectionText(
            title = "2. How data is used",
            bullets = listOf(
                "Resume course progress and compute streaks locally.",
                "Display dictionary cards and HSK summaries without internet access.",
                "Investigate bugs only if you share logs manually.",
            ),
        ),
        PolicySectionText(
            title = "3. Permissions",
            bullets = listOf(
                "INTERNET (optional) reserved for future downloads or external dictionary links.",
                "No access to location, contacts, or advertising IDs.",
            ),
        ),
        PolicySectionText(
            title = "4. Your controls",
            bullets = listOf(
                "Clear the app's storage in Android settings to reset all data.",
                "Use the Privacy dialog to toggle analytics, crash logs, or network prefetch.",
                "Review and edit feedback logs before sending.",
            ),
        ),
        PolicySectionText(
            title = "5. Contact",
            bullets = listOf(
                "Email: qq260316514@gmail.com",
                "GitHub: https://github.com/chanind/hanzi-writer-data (resource reference).",
            ),
        ),
    ),
)

private fun localizedStringsEs(locale: Locale): LocalizedStrings = LocalizedStrings(
    locale = locale,
    appTitle = "Hanzi Stroke Order",
    searchLabel = "Hanzi",
    loadButton = "Cargar carácter",
    clearButton = "Borrar",
    loadingLabel = "Cargando...",
    coursesDialogTitle = "Cursos HSK",
    coursePlanHeading = "Elige un curso para continuar",
    courseIntroTitle = "Cómo funcionan los cursos",
    courseIntroBullets = listOf(
        "Selecciona un nivel HSK para comenzar o retomar el estudio.",
        "Toca un carácter para practicar; mantén pulsado para marcarlo como dominado.",
        "La insignia sobre el tablero refleja tu progreso actual.",
    ),
    courseEmptyTitle = "Sin curso activo",
    courseEmptyDescription = "Selecciona un nivel HSK para iniciar una sesión guiada.",
    courseNoDataMessage = "No se encontraron datos del curso. Comprueba el paquete de recursos.",
    expandCharactersLabel = "Mostrar todo",
    collapseCharactersLabel = "Ocultar",
    legendTitle = "Leyenda y gestos",
    legendGesture = "Toca un carácter para practicar; mantén pulsado para marcarlo como aprendido.",
    legendActiveLabel = "Activo",
    legendCompletedLabel = "Completado",
    legendRemainingLabel = "Pendiente",
    courseLegendHint = "Usa los controles de la insignia para reanudar, saltar, reiniciar o salir sin cubrir el tablero.",
    courseLevelCompleteLabel = "Curso completo",
    courseLevelStartFormat = "Empieza con %s",
    courseLevelNextFormat = "Siguiente • %s",
    levelLabelFormat = "Nivel %d",
    courseLevelProgressFormat = "%1\$d/%2\$d dominados · faltan %3\$d",
    loadLevelFormat = "Abrir HSK %d",
    filterAllLabel = "Todos",
    filterRemainingLabel = "Pendientes",
    filterCompletedLabel = "Completados",
    helpTitle = "Ayuda e introducción",
    helpSections = listOf(
        HelpSectionText(
            title = "Inicio rápido",
            description = "Completa un ciclo de demostración → práctica en segundos.",
            bullets = listOf(
                "Escribe cualquier hanzi y toca el ícono de nube para cargar los datos sin conexión.",
                "Usa Reproducir o Bucle para ver la demostración; activa la plantilla caligráfica para calcar.",
                "Pulsa el lápiz en el tablero para empezar; el ícono de pista resalta el siguiente trazo.",
            ),
        ),
        HelpSectionText(
            title = "Cursos y progreso",
            description = "Los cursos HSK viven bajo el avatar y la insignia muestra tu estado.",
            bullets = listOf(
                "Los botones Reanudar/Saltar/Reiniciar/Salir permanecen en la insignia para no tapar el tablero.",
                "Las flechas cambian de carácter; al terminar uno se actualiza el resumen al instante.",
            ),
        ),
        HelpSectionText(
            title = "Tarjetas y pronunciación",
            description = "La tarjeta en caligrafía muestra el diccionario y la voz sintética.",
            bullets = listOf(
                "Toca la tarjeta para abrir la definición completa y desplazarte por la explicación.",
                "El botón de altavoz reproduce audio en mandarín sin conexión.",
            ),
        ),
        HelpSectionText(
            title = "Cuadrículas y plantillas",
            description = "El tablero admite cuadrícula mi, cuadrícula de nueve o sin guías.",
            bullets = listOf(
                "Cambia entre modo con plantilla o libre; los colores de los trazos se conservan.",
                "Desplázate fuera del lienzo para mover la página y mantener fija la zona de escritura.",
            ),
        ),
        HelpSectionText(
            title = "Soporte y comentarios",
            description = "¿Necesitas ayuda? Escríbenos.",
            bullets = listOf(
                "Correo: qq260316514@gmail.com",
                "Perfil → Feedback permite adjuntar registros antes de enviarlos.",
            ),
        ),
    ),
    helpConfirm = "Entendido",
    privacyTitle = "Preferencias de privacidad",
    privacyIntro = "La app funciona sin conexión por defecto. Activa diagnósticos solo si estás de acuerdo.",
    dataSafetyHeading = "Resumen de seguridad de datos",
    privacySummaryRows = listOf(
        SummaryRowText(
            title = "Almacenamiento",
            detail = "Historial de práctica, progreso y ajustes del tablero permanecen solo en DataStore local.",
        ),
        SummaryRowText(
            title = "Recursos sin conexión",
            detail = "Los paquetes de trazos y cursos vienen en el APK; el prefetch opcional solo funciona si lo habilitas.",
        ),
        SummaryRowText(
            title = "Registros y comentarios",
            detail = "Los registros de texto se adjuntan únicamente cuando envías comentarios.",
        ),
    ),
    contactSupportLabel = "Contacto: %s",
    emailSupportButton = "Enviar correo",
    viewPolicyButton = "Ver política completa",
    fullPolicyTitle = "Política de privacidad",
    fullPolicySections = listOf(
        PolicySectionText(
            title = "1. Datos que recopilamos",
            bullets = listOf(
                "Recursos de caracteres incluidos en el APK para mostrar el orden de los trazos.",
                "Hasta 40 registros locales para reanudar sesiones.",
                "Preferencias del tablero como cuadrículas, colores y plantillas.",
                "Registros de texto opcionales solo cuando envías comentarios.",
            ),
        ),
        PolicySectionText(
            title = "2. Uso de los datos",
            bullets = listOf(
                "Reanudar el progreso del curso y calcular rachas localmente.",
                "Mostrar tarjetas de diccionario y resúmenes HSK sin internet.",
                "Investigar errores solo si compartes registros manualmente.",
            ),
        ),
        PolicySectionText(
            title = "3. Permisos",
            bullets = listOf(
                "INTERNET (opcional) reservado para futuras descargas o enlaces externos.",
                "Sin acceso a ubicación, contactos ni identificadores publicitarios.",
            ),
        ),
        PolicySectionText(
            title = "4. Tus controles",
            bullets = listOf(
                "Borra los datos de la app en Ajustes de Android para reiniciar todo.",
                "Usa el diálogo de Privacidad para activar o desactivar analíticas, registros o prefetch.",
                "Revisa y edita los registros antes de enviarlos.",
            ),
        ),
        PolicySectionText(
            title = "5. Contacto",
            bullets = listOf(
                "Correo: qq260316514@gmail.com",
                "GitHub: https://github.com/chanind/hanzi-writer-data (referencia de recursos).",
            ),
        ),
    ),
)

private fun localizedStringsJa(locale: Locale): LocalizedStrings = LocalizedStrings(
    locale = locale,
    appTitle = "Hanzi Stroke Order",
    searchLabel = "漢字",
    loadButton = "文字を読み込む",
    clearButton = "クリア",
    loadingLabel = "読み込み中…",
    coursesDialogTitle = "HSKコース",
    coursePlanHeading = "学習するコースを選択",
    courseIntroTitle = "コースの使い方",
    courseIntroBullets = listOf(
        "HSK レベルを選んで学習を開始・再開します。",
        "文字をタップして練習、長押しで「習得済み」にします。",
        "ボード上部のバッジが現在の進捗を示します。",
    ),
    courseEmptyTitle = "進行中のコースはありません",
    courseEmptyDescription = "下の HSK レベルから選んでガイド付きセッションを始めましょう。",
    courseNoDataMessage = "コースデータが見つかりません。リソースパックを確認してください。",
    expandCharactersLabel = "すべて表示",
    collapseCharactersLabel = "隠す",
    legendTitle = "凡例と操作",
    legendGesture = "文字をタップして練習、長押しで習得済みにします。",
    legendActiveLabel = "学習中",
    legendCompletedLabel = "完了",
    legendRemainingLabel = "未学習",
    courseLegendHint = "バッジのボタンで再開・スキップ・リスタート・終了を行えます。",
    courseLevelCompleteLabel = "コース完了",
    courseLevelStartFormat = "%s から開始",
    courseLevelNextFormat = "次 • %s",
    levelLabelFormat = "レベル %d",
    courseLevelProgressFormat = "%1\$d/%2\$d 習得 • 残り %3\$d",
    loadLevelFormat = "HSK %d を開く",
    filterAllLabel = "すべて",
    filterRemainingLabel = "未完了",
    filterCompletedLabel = "完了",
    helpTitle = "ヘルプとガイド",
    helpSections = listOf(
        HelpSectionText(
            title = "クイックスタート",
            description = "デモ → 練習の流れを数秒で体験できます。",
            bullets = listOf(
                "任意の漢字を入力し、雲のアイコンでオフラインデータを読み込みます。",
                "再生／ループでデモを確認し、楷書テンプレートでなぞり書きできます。",
                "ボードの鉛筆アイコンで練習開始、ヒントで次の筆画を表示します。",
            ),
        ),
        HelpSectionText(
            title = "コースと進捗",
            description = "プロフィールメニューに HSK コースがあり、バッジが進捗を表示します。",
            bullets = listOf(
                "再開／スキップ／リスタート／終了ボタンはバッジ内に収まり、ボードを邪魔しません。",
                "矢印で文字を切り替え、完了すると即座に統計が更新されます。",
            ),
        ),
        HelpSectionText(
            title = "カードと発音",
            description = "楷書カードには辞書情報と音声再生が含まれます。",
            bullets = listOf(
                "カードをタップすると全文表示され、スクロールで詳細を確認できます。",
                "スピーカーボタンでオフラインの音声 TTS を再生します。",
            ),
        ),
        HelpSectionText(
            title = "グリッドとテンプレート",
            description = "ボードは米字、九宮、なしを切り替え可能です。",
            bullets = listOf(
                "描き取り／フリーモードを切り替えると、色設定は保存されます。",
                "キャンバス外をスクロールして全体を移動し、書画面を安定させます。",
            ),
        ),
        HelpSectionText(
            title = "サポートとフィードバック",
            description = "質問があればいつでもご連絡ください。",
            bullets = listOf(
                "メール: qq260316514@gmail.com",
                "プロフィール → Feedback でログを添付して送信できます。",
            ),
        ),
    ),
    helpConfirm = "了解",
    privacyTitle = "プライバシー設定",
    privacyIntro = "アプリは既定でオフライン動作します。許可する場合のみ診断を有効にしてください。",
    dataSafetyHeading = "データ安全の概要",
    privacySummaryRows = listOf(
        SummaryRowText(
            title = "データ保存",
            detail = "練習履歴・コース進捗・ボード設定は端末の DataStore のみに保存されます。",
        ),
        SummaryRowText(
            title = "オフライン資産",
            detail = "筆順 JSON とコースパックは APK 内に含まれ、プリフェッチは許可しない限り実行されません。",
        ),
        SummaryRowText(
            title = "ログとフィードバック",
            detail = "フィードバック送信時にのみテキストログが添付されます。",
        ),
    ),
    contactSupportLabel = "連絡先: %s",
    emailSupportButton = "メールで問い合わせ",
    viewPolicyButton = "全文を見る",
    fullPolicyTitle = "プライバシーポリシー",
    fullPolicySections = listOf(
        PolicySectionText(
            title = "1. 収集するデータ",
            bullets = listOf(
                "筆順描画のためのリソース（APK 内に同梱）。",
                "最大40件の練習履歴でセッションを再開。",
                "ボードの色・グリッド・テンプレート設定。",
                "フィードバック送信時にのみ添付されるテキストログ。",
            ),
        ),
        PolicySectionText(
            title = "2. 利用目的",
            bullets = listOf(
                "コース進捗の復元とストリーク計算（すべてオフライン）。",
                "辞書カードと HSK 概要の表示。",
                "バグ調査（ログ共有時のみ）。",
            ),
        ),
        PolicySectionText(
            title = "3. 権限",
            bullets = listOf(
                "INTERNET（任意）: 将来の追加ダウンロードや外部辞書リンク用。",
                "位置情報・連絡先・広告 ID にはアクセスしません。",
            ),
        ),
        PolicySectionText(
            title = "4. コントロール",
            bullets = listOf(
                "Android 設定でアプリのデータを消去すると完全にリセットできます。",
                "プライバシーダイアログで分析・クラッシュログ・プリフェッチを切り替え。",
                "送信前にログ内容を確認・編集可能。",
            ),
        ),
        PolicySectionText(
            title = "5. 連絡先",
            bullets = listOf(
                "メール: qq260316514@gmail.com",
                "GitHub: https://github.com/chanind/hanzi-writer-data (リソース参照)。",
            ),
        ),
    ),
)
