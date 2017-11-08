package cz.cuni.mff.fruiton.service.game;

public enum GameResult {

    WIN, LOSE, DRAW;

    public GameResult inverse() {
        switch (this) {
            case WIN:
                return LOSE;
            case LOSE:
                return WIN;
            case DRAW:
                return DRAW;
            default:
                throw new IllegalStateException("Cannot compute inverse of " + this);
        }
    }

}
