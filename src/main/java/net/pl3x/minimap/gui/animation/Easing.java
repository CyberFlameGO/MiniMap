package net.pl3x.minimap.gui.animation;

/*
 *
 * Copyright 2001 Robert Penner
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.pl3x.minimap.util.Mathf;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Easing {
    public static class Back {
        public static final Func in = new Func("back-in", Back::in);
        public static final Func out = new Func("back-out", Back::out);
        public static final Func inOut = new Func("back-in-out", Back::inOut);

        private static final float s = 1.70158F;
        private static final float s2 = 2.5949095F;

        public static float in(float t) {
            return t * t * ((s + 1F) * t - s);
        }

        public static float out(float t) {
            return (t -= 1F) * t * ((s + 1F) * t + s) + 1;
        }

        public static float inOut(float t) {
            if ((t *= 2F) < 1F) return 0.5F * (t * t * ((s2 + 1F) * t - s2));
            return 0.5F * ((t -= 2F) * t * ((s2 + 1F) * t + s2) + 2F);
        }
    }

    public static class Bounce {
        public static final Func in = new Func("bounce-in", Bounce::in);
        public static final Func out = new Func("bounce-out", Bounce::out);
        public static final Func inOut = new Func("bounce-in-out", Bounce::inOut);

        public static float in(float t) {
            return 1F - out(1F - t);
        }

        public static float out(float t) {
            if (t < (1F / 2.75F)) {
                return 7.5625F * t * t;
            } else if (t < (2F / 2.75F)) {
                return 7.5625F * (t -= (1.5F / 2.75F)) * t + 0.75F;
            } else if (t < (2.5F / 2.75F)) {
                return 7.5625F * (t -= (2.25F / 2.75F)) * t + 0.9375F;
            } else {
                return 7.5625F * (t -= (2.625F / 2.75F)) * t + 0.984375F;
            }
        }

        public static float inOut(float t) {
            if (t < 0.5F) return in(t * 2F) * 0.5F;
            return out(t * 2F - 1F) * 0.5F + 0.5F;
        }
    }

    public static class Circular {
        public static final Func in = new Func("circular-in", Circular::in);
        public static final Func out = new Func("circular-out", Circular::out);
        public static final Func inOut = new Func("circular-in-out", Circular::inOut);

        public static float in(float t) {
            return 1F - Mathf.sqrt(1F - t * t);
        }

        public static float out(float t) {
            return Mathf.sqrt(1F - ((t -= 1F) * t));
        }

        public static float inOut(float t) {
            if ((t *= 2F) < 1F) return -0.5F * (Mathf.sqrt(1F - t * t) - 1F);
            return 0.5F * (Mathf.sqrt(1F - (t -= 2F) * t) + 1F);
        }
    }

    public static class Cubic {
        public static final Func in = new Func("cubic-in", Cubic::in);
        public static final Func out = new Func("cubic-out", Cubic::out);
        public static final Func inOut = new Func("cubic-in-out", Cubic::inOut);

        public static float in(float t) {
            return t * t * t;
        }

        public static float out(float t) {
            return 1F + ((t -= 1F) * t * t);
        }

        public static float inOut(float t) {
            if ((t *= 2F) < 1F) return 0.5F * t * t * t;
            return 0.5F * ((t -= 2F) * t * t + 2F);
        }
    }

    public static class Elastic {
        public static final Func in = new Func("elastic-in", Elastic::in);
        public static final Func out = new Func("elastic-out", Elastic::out);
        public static final Func inOut = new Func("elastic-in-out", Elastic::inOut);

        public static float in(float t) {
            if (t == 0F) return 0F;
            if (t == 1F) return 1F;
            return -Mathf.pow(2F, 10F * (t -= 1F)) * Mathf.sin((t - 0.1F) * (2F * Mathf.PI) / 0.4F);
        }

        public static float out(float t) {
            if (t == 0F) return 0F;
            if (t == 1F) return 1F;
            return Mathf.pow(2F, -10F * t) * Mathf.sin((t - 0.1F) * (2F * Mathf.PI) / 0.4F) + 1F;
        }

        public static float inOut(float t) {
            if ((t *= 2F) < 1F)
                return -0.5F * Mathf.pow(2F, 10F * (t -= 1F)) * Mathf.sin((t - 0.1F) * (2F * Mathf.PI) / 0.4F);
            return Mathf.pow(2F, -10F * (t -= 1F)) * Mathf.sin((t - 0.1F) * (2F * Mathf.PI) / 0.4F) * 0.5F + 1F;
        }
    }

    public static class Exponential {
        public static final Func in = new Func("exponential-in", Exponential::in);
        public static final Func out = new Func("exponential-out", Exponential::out);
        public static final Func inOut = new Func("exponential-in-out", Exponential::inOut);

        public static float in(float t) {
            return t == 0F ? 0F : Mathf.pow(1024F, t - 1F);
        }

        public static float out(float t) {
            return t == 1F ? 1F : 1F - Mathf.pow(2F, -10F * t);
        }

        public static float inOut(float t) {
            if (t == 0F) return 0F;
            if (t == 1F) return 1F;
            if ((t *= 2F) < 1F) return 0.5F * Mathf.pow(1024F, t - 1F);
            return 0.5F * (-Mathf.pow(2F, -10F * (t - 1F)) + 2F);
        }
    }

    public static class Quadratic {
        public static final Func in = new Func("quadratic-in", Quadratic::in);
        public static final Func out = new Func("quadratic-out", Quadratic::out);
        public static final Func inOut = new Func("quadratic-in-out", Quadratic::inOut);

        public static float in(float t) {
            return t * t;
        }

        public static float out(float t) {
            return t * (2F - t);
        }

        public static float inOut(float t) {
            if ((t *= 2F) < 1F) return 0.5F * t * t;
            return -0.5F * ((t -= 1F) * (t - 2F) - 1F);
        }
    }

    public static class Quartic {
        public static final Func in = new Func("quartic-in", Quartic::in);
        public static final Func out = new Func("quartic-out", Quartic::out);
        public static final Func inOut = new Func("quartic-in-out", Quartic::inOut);

        public static float in(float t) {
            return t * t * t * t;
        }

        public static float out(float t) {
            return 1F - ((t -= 1F) * t * t * t);
        }

        public static float inOut(float t) {
            if ((t *= 2F) < 1F) return 0.5F * t * t * t * t;
            return -0.5F * ((t -= 2F) * t * t * t - 2F);
        }
    }

    public static class Quintic {
        public static final Func in = new Func("quintic-in", Quintic::in);
        public static final Func out = new Func("quintic-out", Quintic::out);
        public static final Func inOut = new Func("quintic-in-out", Quintic::inOut);

        public static float in(float t) {
            return t * t * t * t * t;
        }

        public static float out(float t) {
            return 1F + ((t -= 1F) * t * t * t * t);
        }

        public static float inOut(float t) {
            if ((t *= 2F) < 1F) return 0.5F * t * t * t * t * t;
            return 0.5F * ((t -= 2F) * t * t * t * t + 2F);
        }
    }

    public static class Sinusoidal {
        public static final Func in = new Func("sinusoidal-in", Sinusoidal::in);
        public static final Func out = new Func("sinusoidal-out", Sinusoidal::out);
        public static final Func inOut = new Func("sinusoidal-in-out", Sinusoidal::inOut);

        public static float in(float t) {
            return 1F - Mathf.cos(t * Mathf.PI / 2F);
        }

        public static float out(float t) {
            return Mathf.sin(t * Mathf.PI / 2F);
        }

        public static float inOut(float t) {
            return 0.5F * (1F - Mathf.cos(Mathf.PI * t));
        }
    }

    public static class Adapter implements JsonSerializer<Func>, JsonDeserializer<Func> {
        @Override
        public JsonElement serialize(Easing.Func func, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(func.name);
        }

        @Override
        public Easing.Func deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            return Func.BY_NAME.get(json.getAsString());
        }
    }

    public record Func(String name, Easing.Func.EasingFunc func) {
        private static final Map<String, Func> BY_NAME = new HashMap<>();

        public Func(String name, EasingFunc func) {
            this.name = name;
            this.func = func;

            BY_NAME.put(name, this);
        }

        public float apply(float t) {
            return func.apply(t);
        }

        @FunctionalInterface
        private interface EasingFunc {
            float apply(float t);
        }
    }
}
