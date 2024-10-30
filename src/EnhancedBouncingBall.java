import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnhancedBouncingBall extends JPanel implements ActionListener {
    private List<Ball> balls = new ArrayList<>();
    private List<Boolean> circleActive; // Track active circles
    private int ballCount;
    private int circleCount; // Number of circles
    private Timer timer;
    static boolean growOnHit;
    static int ballSize;

    public EnhancedBouncingBall(int ballCount, int circleCount, boolean growOnHit, int ballSize, Color[] ballColors) {
        this.ballCount = ballCount;
        this.circleCount = circleCount; // Set circle count
        EnhancedBouncingBall.growOnHit = growOnHit;
        EnhancedBouncingBall.ballSize = ballSize;

        circleActive = new ArrayList<>(circleCount);
        for (int i = 0; i < circleCount; i++) {
            circleActive.add(true); // All circles start active
        }

        // Add balls starting from the center of the circles with random velocities
        for (Color color : ballColors) {
            balls.add(new Ball(color)); 
        }

        timer = new Timer(10, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawRotatingCircles(g);  // Draw circles first
        for (Ball ball : balls) {
            ball.draw(g);  // Draw balls on top
        }
    }

    private void drawRotatingCircles(Graphics g) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int baseRadius = 2 * ballSize; // Base radius for circles

        for (int i = 0; i < circleCount; i++) {
            if (!circleActive.get(i)) continue; // Skip inactive circles
            
            int radius = baseRadius * (i + 1);
            double holeAngle = System.currentTimeMillis() * 0.002 * (i + 1); // Different speed for each hole

            // Draw the circle outline with thickness
            g.setColor(Color.LIGHT_GRAY);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(2)); // Adjust thickness slightly
            g2d.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

            // Calculate the position of the hole
            int holeX = (int) (centerX + radius * Math.cos(holeAngle));
            int holeY = (int) (centerY + radius * Math.sin(holeAngle));

            // Draw the hole (gap in the circle)
            g.setColor(getBackground());
            g.fillOval(holeX - (int)(ballSize * 2 / 2), holeY - (int)(ballSize * 2 / 2), (int)(ballSize * 2), (int)(ballSize * 2)); // Hole size
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Ball ball : balls) {
            ball.update();
        }
        repaint();
    }

    public static void main(String[] args) {
        // Input dialog for number of balls
        String ballsInput = JOptionPane.showInputDialog("Enter the number of balls to display:");
        int selectedBalls = Integer.parseInt(ballsInput);

        // Input dialog for number of circles
        String circlesInput = JOptionPane.showInputDialog("Enter the number of circles to display:");
        int selectedCircles = Integer.parseInt(circlesInput);

        // Input dialog for growth option
        int response = JOptionPane.showConfirmDialog(null, "Enable ball growth on hit?", "Select Option",
                JOptionPane.YES_NO_OPTION);
        boolean growOnHit = (response == JOptionPane.YES_OPTION);

        // Input dialog for ball size
        String[] options = {"Small", "Medium", "Large"};
        int sizeResponse = JOptionPane.showOptionDialog(null, "Select the size of the balls:", "Ball Size",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        int ballSize;
        switch (sizeResponse) {
            case 0: // Small
                ballSize = 20;
                break;
            case 1: // Medium
                ballSize = 40;
                break;
            case 2: // Large
                ballSize = 60;
                break;
            default: // Default to small if canceled
                ballSize = 20;
                break;
        }

        // Color selection for each ball on one screen
        Color[] ballColors = new Color[selectedBalls];
        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new GridLayout(selectedBalls + 1, 2)); // One extra row for Random Color button
        for (int i = 0; i < selectedBalls; i++) {
            JLabel label = new JLabel("Choose color for ball " + (i + 1) + ":");
            JButton colorButton = new JButton("Select Color");
            int index = i; // To use in the action listener
            colorButton.addActionListener(e -> {
                Color selectedColor = JColorChooser.showDialog(null, "Choose color for ball " + (index + 1), Color.CYAN);
                ballColors[index] = (selectedColor != null) ? selectedColor : Color.CYAN; // Default to cyan if canceled
                colorButton.setBackground(ballColors[index]);
            });
            colorPanel.add(label);
            colorPanel.add(colorButton);
        }

        // Add Random Color button
        JButton randomColorButton = new JButton("Randomize Colors");
        randomColorButton.addActionListener(e -> {
            Random rand = new Random();
            for (int i = 0; i < ballColors.length; i++) {
                if (ballColors[i] == null) { // Only override if color not selected
                    ballColors[i] = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)); // Random color
                    ((JButton) colorPanel.getComponent(i * 2 + 1)).setBackground(ballColors[i]); // Update button background
                }
            }
        });
        colorPanel.add(new JLabel("")); // Empty cell
        colorPanel.add(randomColorButton);

        int option = JOptionPane.showConfirmDialog(null, colorPanel, "Select Colors for Balls", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) {
            System.exit(0); // Exit if canceled
        }

        // Create the window and start the animation
        JFrame frame = new JFrame("Enhanced Bouncing Ball");
        EnhancedBouncingBall bouncingBall = new EnhancedBouncingBall(selectedBalls, selectedCircles, growOnHit, ballSize, ballColors);
        frame.add(bouncingBall);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Inner class for Ball
    class Ball {
        private int x, y, velocityX, velocityY, size;
        private Color color;

        public Ball(Color color) { // Accept color
            this.color = color; // Set color
            x = getWidth() / 2; // Start from center of the circles
            y = getHeight() / 2;
            double angle = Math.random() * 2 * Math.PI; // Random angle
            double speed = 2 + Math.random() * 4; // Random speed
            this.size = ballSize; // Set ball size based on user choice
            
            // Set velocities based on the random angle
            velocityX = (int) (speed * Math.cos(angle));
            velocityY = (int) (speed * Math.sin(angle));
        }

        public void draw(Graphics g) {
            g.setColor(color);
            g.fillOval(x, y, size, size);
        }

        public void update() {
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            int baseRadius = 2 * ballSize; // Base radius for circles

            // Update ball position
            x += velocityX;
            y += velocityY;

            // Check collisions with circle outlines
            for (int i = 0; i < balls.size(); i++) {
                if (!circleActive.get(i)) continue; // Skip inactive circles

                int radius = baseRadius * (i + 1);
                double dist = Math.sqrt(Math.pow(centerX - x - size / 2, 2) + Math.pow(centerY - y - size / 2, 2));
                double outlineRadius = radius; // Circle outline hitbox
                
                // Calculate the position of the hole
                double holeAngle = System.currentTimeMillis() * 0.002 * (i + 1);
                int holeX = (int) (centerX + radius * Math.cos(holeAngle));
                int holeY = (int) (centerY + radius * Math.sin(holeAngle));
                int holeSize = (int) (ballSize * 2);

                // If the ball is in the hole, deactivate the circle
                if (Math.abs(x + size / 2 - holeX) < holeSize / 2 && Math.abs(y + size / 2 - holeY) < holeSize / 2) {
                    circleActive.set(i, false); // Deactivate the circle
                    return; // Ball passes through the hole, do nothing
                }

                // Ball edge collision
                if (dist <= outlineRadius && dist >= outlineRadius - size / 2) {
                    // Bounce off the circle outline
                    if (x + size / 2 < centerX) {
                        velocityX = Math.abs(velocityX); // Move right
                    } else {
                        velocityX = -Math.abs(velocityX); // Move left
                    }
                    if (y + size / 2 < centerY) {
                        velocityY = Math.abs(velocityY); // Move down
                    } else {
                        velocityY = -Math.abs(velocityY); // Move up
                    }
                    return; // Exit after collision
                }
            }

            // Collision detection with walls
            if (x + size > getWidth() || x < 0) {
                velocityX = -velocityX;
                if (growOnHit) {
                    size += 5;
                }
            }
            if (y + size > getHeight() || y < 0) {
                velocityY = -velocityY;
                if (growOnHit) {
                    size += 5;
                }
            }
        }
    }
}
