import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;


public class CatchDropGameApp extends GameApplication {
    private static final int H = 650;
    private static final int W = 600;

    public enum Type {
        DROPLET, BUCKET, GRENADE
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("playerScore", 0);
        vars.put("playerHeal", 3);
        vars.put("playerLostScore", 0);
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Catch Drop Game");
        settings.setWidth(W);
        settings.setHeight(H);
        settings.setVersion("1.0");
        settings.setNative(true);
    }
    @Override
    protected void initUI() {
        getGameScene().setBackgroundRepeat("forest.png");

        Text score = new Text();
        score.setFill(Color.BLUE);
        score.setStyle("-fx-font: 30 arial;");
        score.setTranslateX(85);
        score.setTranslateY(58);
        score.textProperty().bind(getWorldProperties().intProperty("playerScore").asString());
        getGameScene().addUINode(score);

        var dropletTexture = getAssetLoader().loadTexture("droplet.png");
        dropletTexture.setTranslateX(45);
        dropletTexture.setTranslateY(25);
        dropletTexture.setFitWidth(25);
        dropletTexture.setFitHeight(38);
        getGameScene().addUINode(dropletTexture);

        Text heal = new Text();
        heal.setFill(Color.RED);
        heal.setStyle("-fx-font: 30 arial;");
        heal.setTranslateX(85);
        heal.setTranslateY(108);
        heal.textProperty().bind(getWorldProperties().intProperty("playerHeal").asString());
        getGameScene().addUINode(heal);

        Text lostScore = new Text();
        lostScore.setFill(Color.GREEN);
        lostScore.setStyle("-fx-font: 30 arial;");
        lostScore.setTranslateX(85);
        lostScore.setTranslateY(148);
        lostScore.textProperty().bind(getWorldProperties().intProperty("playerLostScore").asString());
        getGameScene().addUINode(lostScore);
    }

    @Override
    protected void initGame() {
        spawnBucket();
        run(() -> spawnDroplet(), Duration.seconds(1.0));
        run(() -> spawnGrenade(), Duration.seconds(5.0 ));

        loopBGM("bgm.mp3");
    }

    @Override
    protected void onUpdate(double tpf) {
        // for each entity of Type.DROPLET translate (move) it down
        getGameWorld().getEntitiesByType(Type.DROPLET).forEach(droplet -> droplet.translateY(150 * tpf));
        getGameWorld().getEntitiesByType(Type.GRENADE).forEach(droplet -> droplet.translateY(100 * tpf));
        getGameWorld().getEntitiesByType(Type.DROPLET).forEach(droplet -> {
            if (droplet.getY() == H) {
                droplet.removeFromWorld();
                inc("playerLostScore", +1);

                int lostScore = getWorldProperties().getValue("playerLostScore");
                if (lostScore == 30) {
                    showGameOver(getWorldProperties().getValue("playerScore"));
                }
            }
        });


    }

    @Override
    protected void initPhysics() {
        onCollisionBegin(Type.BUCKET, Type.DROPLET, (bucket, droplet) -> {
            droplet.removeFromWorld();
            play("drop.wav");
            inc("playerScore", +1);
        });
        onCollisionBegin(Type.BUCKET, Type.GRENADE, (bucket, grenade) -> {
            grenade.removeFromWorld();
            play("123.wav");
            inc("playerHeal", -1);

            int heal = getWorldProperties().getValue("playerHeal");
            if (heal == 0) {
                showGameOver(getWorldProperties().getValue("playerScore"));
            }
        });
    }



    private void spawnBucket() {
        // build an entity with Type.BUCKET
        // at the position X = getAppWidth() / 2 and Y = getAppHeight() - 200
        // with a view "bucket.png", which is an image located in /resources/assets/textures/
        // also create a bounding box from that view
        // make the entity collidable
        // finally, complete building and attach to the game world

        Entity bucket = entityBuilder()
                .type(Type.BUCKET)
                .at(getAppWidth() / 2, getAppHeight() - 200)
                .viewWithBBox("bucket.png")
                .collidable()
                .buildAndAttach();

        // bind bucket's X value to mouse X
        bucket.xProperty().bind(getInput().mouseXWorldProperty());
    }

    private void spawnDroplet() {
        entityBuilder()
                .type(Type.DROPLET)
                .at(FXGLMath.random(0, getAppWidth() - 64), 0)
                .viewWithBBox("droplet.png")
                .collidable()
                .buildAndAttach();
    }

    private void spawnGrenade() {
        entityBuilder()
                .type(Type.GRENADE)
                .at(FXGLMath.random(0, getAppWidth() - 64), 0)
                .viewWithBBox("grenade.png")
                .collidable()
                .buildAndAttach();
    }

    private void showGameOver(int score) {
        getDialogService().showMessageBox("Game over. Your score: " + score, getGameController()::exit);
    }

    public static void main(String[] args) {
        launch(args);
    }
}