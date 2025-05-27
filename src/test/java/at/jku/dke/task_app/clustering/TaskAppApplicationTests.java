package at.jku.dke.task_app.clustering;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(DatabaseSetupExtension.class)
class TaskAppApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void runs() {
        TaskAppApplication.main(new String[0]);
    }

}
