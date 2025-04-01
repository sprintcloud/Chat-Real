module cn.ckaiz.chat_real {
    requires javafx.controls;
    requires javafx.fxml;
    
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires redis.clients.jedis;

    opens cn.ckaiz.chat_real to javafx.fxml;
    exports cn.ckaiz.chat_real;
}