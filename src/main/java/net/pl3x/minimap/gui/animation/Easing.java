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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Easing {
    public static final float PI = 3.14159265358979323846F;

    public static float cos(float t) {
        return (float) Math.cos(t);
    }

    public static float pow(float t, float p) {
        return (float) Math.pow(t, p);
    }

    public static float sin(float t) {
        return (float) Math.sin(t);
    }

    public static float sqrt(float t) {
        return (float) Math.sqrt(t);
    }

    public static class Back {
        public static final Func in = new Func("back-in", Back::in);
        public static final Func out = new Func("back-out", Back::out);
        public static final Func inOut = new Func("back-in-out", Back::inOut);

        private static final float s = 1.70158f;
        private static final float s2 = 2.5949095f;

        public static float in(float t) {
            return t * t * ((s + 1f) * t - s);
        }

        public static float out(float t) {
            return (t -= 1f) * t * ((s + 1f) * t + s) + 1f;
        }

        public static float inOut(float t) {
            if ((t *= 2f) < 1f) return 0.5f * (t * t * ((s2 + 1f) * t - s2));
            return 0.5f * ((t -= 2f) * t * ((s2 + 1f) * t + s2) + 2f);
        }
    }

    public static class Bounce {
        public static final Func in = new Func("bounce-in", Bounce::in);
        public static final Func out = new Func("bounce-out", Bounce::out);
        public static final Func inOut = new Func("bounce-in-out", Bounce::inOut);

        public static float in(float t) {
            return 1f - out(1f - t);
        }

        public static float out(float t) {
            if (t < (1f / 2.75f)) {
                return 7.5625f * t * t;
            } else if (t < (2f / 2.75f)) {
                return 7.5625f * (t -= (1.5f / 2.75f)) * t + 0.75f;
            } else if (t < (2.5f / 2.75f)) {
                return 7.5625f * (t -= (2.25f / 2.75f)) * t + 0.9375f;
            } else {
                return 7.5625f * (t -= (2.625f / 2.75f)) * t + 0.984375f;
            }
        }

        public static float inOut(float t) {
            if (t < 0.5f) return in(t * 2f) * 0.5f;
            return out(t * 2f - 1f) * 0.5f + 0.5f;
        }
    }

    public static class Circular {
        public static final Func in = new Func("circular-in", Circular::in);
        public static final Func out = new Func("circular-out", Circular::out);
        public static final Func inOut = new Func("circular-in-out", Circular::inOut);

        public static float in(float t) {
            return 1f - sqrt(1f - t * t);
        }

        public static float out(float t) {
            return sqrt(1f - ((t -= 1f) * t));
        }

        public static float inOut(float t) {
            if ((t *= 2f) < 1f) return -0.5f * (sqrt(1f - t * t) - 1);
            return 0.5f * (sqrt(1f - (t -= 2f) * t) + 1f);
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
            return 1f + ((t -= 1f) * t * t);
        }

        public static float inOut(float t) {
            if ((t *= 2f) < 1f) return 0.5f * t * t * t;
            return 0.5f * ((t -= 2f) * t * t + 2f);
        }
    }

    public static class Elastic {
        public static final Func in = new Func("elastic-in", Elastic::in);
        public static final Func out = new Func("elastic-out", Elastic::out);
        public static final Func inOut = new Func("elastic-in-out", Elastic::inOut);

        public static float in(float t) {
            if (t == 0) return 0;
            if (t == 1) return 1;
            return -pow(2f, 10f * (t -= 1f)) * sin((t - 0.1f) * (2f * PI) / 0.4f);
        }

        public static float out(float t) {
            if (t == 0) return 0;
            if (t == 1) return 1;
            return pow(2f, -10f * t) * sin((t - 0.1f) * (2f * PI) / 0.4f) + 1f;
        }

        public static float inOut(float t) {
            if ((t *= 2f) < 1f)
                return -0.5f * pow(2f, 10f * (t -= 1f)) * sin((t - 0.1f) * (2f * PI) / 0.4f);
            return pow(2f, -10f * (t -= 1f)) * sin((t - 0.1f) * (2f * PI) / 0.4f) * 0.5f + 1f;
        }
    }

    public static class Exponential {
        public static final Func in = new Func("exponential-in", Exponential::in);
        public static final Func out = new Func("exponential-out", Exponential::out);
        public static final Func inOut = new Func("exponential-in-out", Exponential::inOut);

        public static float in(float t) {
            return t == 0f ? 0f : pow(1024f, t - 1f);
        }

        public static float out(float t) {
            return t == 1f ? 1f : 1f - pow(2f, -10f * t);
        }

        public static float inOut(float t) {
            if (t == 0f) return 0f;
            if (t == 1f) return 1f;
            if ((t *= 2f) < 1f) return 0.5f * pow(1024f, t - 1f);
            return 0.5f * (-pow(2f, -10f * (t - 1f)) + 2f);
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
            return t * (2f - t);
        }

        public static float inOut(float t) {
            if ((t *= 2f) < 1f) return 0.5f * t * t;
            return -0.5f * ((t -= 1f) * (t - 2f) - 1f);
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
            return 1f - ((t -= 1f) * t * t * t);
        }

        public static float inOut(float t) {
            if ((t *= 2f) < 1f) return 0.5f * t * t * t * t;
            return -0.5f * ((t -= 2f) * t * t * t - 2f);
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
            return 1f + ((t -= 1f) * t * t * t * t);
        }

        public static float inOut(float t) {
            if ((t *= 2f) < 1f) return 0.5f * t * t * t * t * t;
            return 0.5f * ((t -= 2f) * t * t * t * t + 2f);
        }
    }

    public static class Sinusoidal {
        public static final Func in = new Func("sinusoidal-in", Sinusoidal::in);
        public static final Func out = new Func("sinusoidal-out", Sinusoidal::out);
        public static final Func inOut = new Func("sinusoidal-in-out", Sinusoidal::inOut);

        public static float in(float t) {
            return 1f - cos(t * PI / 2f);
        }

        public static float out(float t) {
            return sin(t * PI / 2f);
        }

        public static float inOut(float t) {
            return 0.5f * (1f - cos(PI * t));
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
