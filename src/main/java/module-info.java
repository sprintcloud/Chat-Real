module cn.ckaiz.chat_real {
    requires javafx.controls;
    requires javafx.fxml;
    
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    
    opens cn.ckaiz.chat_real to javafx.fxml;
    exports cn.ckaiz.chat_real;
}