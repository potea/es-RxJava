
class MainApplication : MvvmfxEasyDIApplication() {

    private val layoutName = "main.fxml"
    private val title = "AcidRain"
    private val xPercent:Double = 50/100.0
    private val yPercent:Double = 50/100.0

    @Throws(Exception::class)
    override fun startMvvmfx(primaryStage: Stage) {
        val fxmlLoader = FXMLLoader(ResourceHelper.convertURL(layoutName))
        val root = fxmlLoader.load<Parent>()

        primaryStage.title = title

        val point = GraphicUtils.getDisplay()

        primaryStage.scene = Scene(root,point.x*xPercent,point.y*yPercent)

        println("${primaryStage.scene.width} , ${primaryStage.scene .height} ")
        primaryStage.show()
    }
}
