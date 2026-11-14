package powerband.model;

/**
 * Un comando como se guarda en disco: "en el milisegundo X desde que empezó la grabación,
 * se invocó este synth con estos parámetros". El tiempo es RELATIVO, por eso al reproducir
 * suena igual sin importar cuándo se cargó la música.
 */
public record CommandRecord(long timeMs, SynthType synth, Params params) {
}
