package org.client.abstraction;


import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public abstract class GUI extends JFrame {
    private int POS_X;
    private int POS_Y;
    private int WIDTH;
    private int HEIGHT;


    public int getPOS_X() {
        return POS_X;
    }

    public int getPOS_Y() {
        return POS_Y;
    }

    public int getWIDTH() {
        return WIDTH;
    }

    public int getHEIGHT() {
        return HEIGHT;
    }

    public void initConstants(int POS_X, int POS_Y, int WIDTH, int HEIGHT){
        this.POS_X = POS_X;
        this.POS_Y = POS_Y;
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
    }


    public static class GUIBuilder {

        private LayoutManager layout;
        private final JPanel panel;
        private final java.util.List<ComponentConfig> components;

        public GUIBuilder() {
            layout = new GridBagLayout();
            panel = new JPanel(layout);
            components = new ArrayList<>();
        }

        public GUIBuilder setLayout(LayoutManager layout) {
            this.layout = layout;
            this.panel.setLayout(layout);
            return this;
        }

        public GUIBuilder addComponent(Component component, int position, int stage, float weightX, float weightY) {

            GridBagConstraints gbc = gbcConfig(position, stage, weightX, weightY);
            components.add(new ComponentConfig(component, gbc));
            return this;
        }

        public GUIBuilder addComponent(@NotNull JButton component, ActionListener l, int position, int stage, float weightX, float weightY) {

            GridBagConstraints gbc = gbcConfig(position, stage, weightX, weightY);
            component.addActionListener(l);
            components.add(new ComponentConfig(component, gbc));
            return this;
        }

        public GUIBuilder addComponent(@NotNull JTextField component, ActionListener l, int position, int stage, float weightX, float weightY) {

            GridBagConstraints gbc = gbcConfig(position, stage, weightX, weightY);
            component.addActionListener(l);
            components.add(new ComponentConfig(component, gbc));
            return this;
        }

        public GUIBuilder addComponent(@NotNull JComboBox<String> component, ActionListener l, int position, int stage, float weightX, float weightY) {

            GridBagConstraints gbc = gbcConfig(position, stage, weightX, weightY);
            component.addActionListener(l);
            components.add(new ComponentConfig(component, gbc));
            return this;
        }

        @NotNull
        private GridBagConstraints gbcConfig(int position, int stage, float weightX, float weightY){

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = position;
            gbc.gridy = stage;
            gbc.weightx = weightX;
            gbc.weighty = weightY;
            return gbc;
        }

        public JPanel build() {

            for (ComponentConfig config : components) {
                panel.add(config.component, config.constraints);
            }
            return panel;
        }

        private record ComponentConfig(Component component, GridBagConstraints constraints) {
        }

    }

    public Component createHeaderOfWindow() {
        return null;
    }

    public Component createBodyOfWindow() {
        return null;
    }

    public Component createFooterOfWindow() {
        return null;
    }
}
