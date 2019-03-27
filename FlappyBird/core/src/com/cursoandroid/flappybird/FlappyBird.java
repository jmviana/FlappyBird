package com.cursoandroid.flappybird;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import android.*;


import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import javax.naming.Context;

import sun.awt.AppContext;

public class FlappyBird extends ApplicationAdapter {

	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Random random;
	private BitmapFont fonte;
	private BitmapFont mensagem;
	private Circle passaroCirculo;
	private Rectangle retanguloCanoTopo;
	private Rectangle retanguloCanoBaixo;
	//private ShapeRenderer shape;

	//Atributos de configuração
	private float larguraDispositivo;
	private float alturaDispositivo;

	private int estadoJogo = 0; //0-> jogo não iniciado  1-> jogo iniciado  2-> jogo Game Over
	private int pontuacao = 0;
	private boolean marcouPonto = false;

	private float larguraPassaro;
	private float alturaPassaro;

	private float variacao = 0;
	private float velocidadeQueda = 0;

	private float posicaoPassaroVertical;
	private float posicaoPassaroHorizontal;

	private float posicaoMovimentoCanoHorizontal;
	private float espacoEntreCanos;

	private float larguraCano;
	private float alturaCano;

	private float deltaTime;

	private float alturaCanosAleatoria;

	private float larguraGameOver;
	private float alturaGameOver;

	//Camera
    private OrthographicCamera camera;
    private Viewport viewport;

    private final float VIRTUAL_WIDTH = 1080;
    private final float VIRTUAL_HEIGHT = 1920;

	@Override
	public void create () {

		batch = new SpriteBatch();
		random = new Random();
		passaroCirculo = new Circle();
		/*retanguloCanoBaixo = new Rectangle();
		retanguloCanoTopo = new Rectangle();
        shape = new ShapeRenderer();*/
		fonte = new BitmapFont();
		fonte.setColor(Color.WHITE);
		fonte.getData().setScale(10);

        mensagem = new BitmapFont();
        mensagem.setColor(Color.WHITE);
        mensagem.getData().setScale(6);

		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");
		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;

		larguraPassaro = larguraDispositivo/7;
		alturaPassaro = larguraDispositivo/10;

		posicaoPassaroVertical = alturaDispositivo/2;
		posicaoPassaroHorizontal = larguraDispositivo/6;

		larguraCano = larguraPassaro * 1.5f;
		alturaCano = alturaDispositivo;

		posicaoMovimentoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = alturaPassaro * 5;

		larguraGameOver = gameOver.getWidth() * 2;
		alturaGameOver = gameOver.getHeight() * 2;

		marcouPonto = false;

		//Configuração da camera
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

	}

	@Override
	public void render () {

	    camera.update();

	    //Limpar frames anteriores
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        deltaTime = Gdx.graphics.getDeltaTime();
        variacao += deltaTime * 10;
        if (variacao >= 3) { variacao = 0; }

	    if (estadoJogo == 0){ //Jogo não iniciado
	        if (Gdx.input.justTouched()){
	            estadoJogo = 1;
            }
        }else { //Jogo iniciado

            velocidadeQueda+=1.5f;
            if (posicaoPassaroVertical > 0 || velocidadeQueda < 0) {
                posicaoPassaroVertical -= velocidadeQueda;
            }

            if (estadoJogo == 1){

                posicaoMovimentoCanoHorizontal -= deltaTime * 400;

                if (Gdx.input.justTouched()) {
                    velocidadeQueda = -32;
                }

                //Verifica se o cano saiu inteiramente da tela
                if (posicaoMovimentoCanoHorizontal < -larguraCano) {
                    posicaoMovimentoCanoHorizontal = larguraDispositivo;
                    alturaCanosAleatoria = random.nextInt((int) (alturaCano - espacoEntreCanos * 1.5f)) - ((int) (alturaCano - espacoEntreCanos * 1.5f)) / 2;
                    marcouPonto = false;
                }

                //Verifica a pontuação
                if (posicaoMovimentoCanoHorizontal < posicaoPassaroHorizontal){
                    if (!marcouPonto){
                        pontuacao++;
                        marcouPonto = true;
                    }

                }

            }else{ //Tela Game Over

                if (Gdx.input.justTouched()){
                    estadoJogo = 0;
                    pontuacao = 0;
                    velocidadeQueda = 0;
                    variacao = 0;
                    marcouPonto = false;
                    posicaoPassaroVertical = alturaDispositivo/2;
                    posicaoMovimentoCanoHorizontal = larguraDispositivo;
                }

            }

        }

        //Configurar dados de projeção da camera
        batch.setProjectionMatrix(camera.combined);

		batch.begin();

		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(canoTopo, posicaoMovimentoCanoHorizontal, alturaDispositivo/2 + espacoEntreCanos/2 + alturaCanosAleatoria, larguraCano, alturaCano);
		batch.draw(canoBaixo, posicaoMovimentoCanoHorizontal, alturaDispositivo/2 - alturaCano - espacoEntreCanos/2 + alturaCanosAleatoria, larguraCano, alturaCano);
		batch.draw(passaros[(int) variacao], posicaoPassaroHorizontal, posicaoPassaroVertical, larguraPassaro, alturaPassaro);
        fonte.draw(batch, String.valueOf(pontuacao), larguraDispositivo/2 - larguraPassaro/4, alturaDispositivo - alturaPassaro);

        if (estadoJogo == 2){
            batch.draw(gameOver, larguraDispositivo/2 - larguraGameOver/2, alturaDispositivo/2, larguraGameOver, alturaGameOver);
            mensagem.draw(batch, "Touch to Restart!", larguraDispositivo/2 - larguraDispositivo/3.4f, alturaDispositivo/2 - alturaGameOver/2);
        }

		batch.end();

		passaroCirculo.set(posicaoPassaroHorizontal + larguraPassaro/2, posicaoPassaroVertical + alturaPassaro/2, (larguraPassaro+alturaPassaro)/4);
        retanguloCanoBaixo = new Rectangle(posicaoMovimentoCanoHorizontal, alturaDispositivo/2 - alturaCano - espacoEntreCanos/2 + alturaCanosAleatoria, larguraCano, alturaCano);
        retanguloCanoTopo = new Rectangle(posicaoMovimentoCanoHorizontal, alturaDispositivo/2 + espacoEntreCanos/2 + alturaCanosAleatoria, larguraCano, alturaCano);

		//Desenhar formas
        /*
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.circle(passaroCirculo.x, passaroCirculo.y, passaroCirculo.radius);
        shape.rect(retanguloCanoBaixo.x, retanguloCanoBaixo.y, retanguloCanoBaixo.width, retanguloCanoBaixo.height);
        shape.rect(retanguloCanoTopo.x, retanguloCanoTopo.y, retanguloCanoTopo.width, retanguloCanoTopo.height);
        shape.setColor(Color.RED);
        shape.end();
        */

        //Teste de colisão
        if (Intersector.overlaps(passaroCirculo, retanguloCanoBaixo) || Intersector.overlaps(passaroCirculo, retanguloCanoTopo)
                || posicaoPassaroVertical <= 0 || posicaoPassaroVertical >= alturaDispositivo - alturaPassaro){
            estadoJogo = 2;
        }

	}


	@Override
	public void dispose () {
		batch.dispose();
		passaros[0].dispose();
		passaros[1].dispose();
		passaros[2].dispose();
	}

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

}
