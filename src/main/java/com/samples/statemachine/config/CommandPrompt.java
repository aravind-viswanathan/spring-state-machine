package com.samples.statemachine.config;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class CommandPrompt implements PromptProvider {
    @Override
    public AttributedString getPrompt() {
        return new AttributedString("state-machine:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }
}
