
import javafx.beans.property.ReadOnlyDoubleWrapper
import javafx.beans.property.ReadOnlyIntegerWrapper
import javafx.beans.property.SimpleIntegerProperty

interface VerticalMoveable {
    val positionY: ReadOnlyDoubleWrapper
    val speed: SimpleIntegerProperty
}
