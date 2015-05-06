package br.com.pos.bancas.service;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.server.sun.http.HttpContextBuilder;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.test.TestPortProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import br.com.pos.banca.constante.Acao;
import br.com.pos.banca.dao.PeriodoAcessoDao;
import br.com.pos.banca.dao.PeriodoAcessoDao;
import br.com.pos.banca.dao.PeriodoAcessoDao;
import br.com.pos.banca.entidade.PeriodoAcesso;
import br.com.pos.banca.entidade.PeriodoAcesso;
import br.com.pos.banca.entidade.PeriodoAcesso;
import br.com.pos.persistencia.Paginacao;

public class PeriodoAcessoServiceTeste {
	
	private Client cliente;
	private EntityManager manager;
	private EntityManagerFactory factory;
	
	private HttpServer server;
	private HttpContextBuilder builder;
	
	@Before
	public void iniciar() throws IOException {
		factory = Persistence.createEntityManagerFactory("bancas");
		manager = factory.createEntityManager();
		cliente = ClientBuilder.newClient();
		
		int porta = TestPortProvider.getPort();
		
		server = HttpServer.create(new InetSocketAddress(porta), 10);
		ResourceFactory resourceFactory = new PeriodoAcessoServiceFactory(manager);

		builder = new HttpContextBuilder();
		builder.bind(server);

		builder.getDeployment().getRegistry().addResourceFactory(resourceFactory);
		server.start();
	}
	
	@After
	public void terminar() {
		cliente.close(); 
		manager.close();
		factory.close();
		
		builder.cleanup();
		server.stop(0);
	}
	
	/**
	 * Metodo responsavel por instanciar e preencher um PeriodoAcesso com uma acao de CADASTRAMENTO
	 * @return PeriodoAcesso
	 * @throws ParseException 
	 */
	private PeriodoAcesso preencherPeriodoAcessoCadastramento() throws ParseException {
		PeriodoAcesso periodoAcesso = new PeriodoAcesso();
		periodoAcesso.setCodigo(Acao.CADASTRAMENTO);
		
		periodoAcesso.setInicio(this.preencherCalendar("01/01/2015 19:00"));
		periodoAcesso.setTermino(this.preencherCalendar("01/01/2015 19:50"));
		
		return periodoAcesso;
	}
	
	/**
	 * Metodo responsavel por instanciar e preencher um PeriodoAcesso com uma acao de ESCOLHA
	 * @return PeriodoAcesso
	 * @throws ParseException 
	 */
	private PeriodoAcesso preencherPeriodoAcessoEscolha() throws ParseException {
		PeriodoAcesso periodoAcesso = new PeriodoAcesso();
		periodoAcesso.setCodigo(Acao.ESCOLHA);
		
		periodoAcesso.setInicio(this.preencherCalendar("01/01/2015 20:00"));
		periodoAcesso.setTermino(this.preencherCalendar("01/01/2015 20:50"));
		
		return periodoAcesso;
	}
	
	/**
	 * Metodo responsavel por preencher um Calendar atraves de uma String.
	 * @return Calendar
	 * @throws ParseException 
	 */
	private Calendar preencherCalendar(String formato) throws ParseException {
		DateFormat diaHorario = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
		diaHorario.parse(formato);
		
		return diaHorario.getCalendar();
	}
	
	/**
	 * Metodo responsavel por testar o metodo que busca um PeriodoAcesso atraves de um exemplo.
	 * @throws Exception
	 */
	@Test
	public void buscar() throws Exception {
		PeriodoAcesso periodoAcesso = preencherPeriodoAcessoCadastramento();
		PeriodoAcessoDao periodoAcessoDao = new PeriodoAcessoDao(manager);
		periodoAcessoDao.persistir(periodoAcesso);
		
		PeriodoAcesso exemplo = new PeriodoAcesso();
		exemplo.setCodigo(Acao.CADASTRAMENTO);;
		
		Collection<PeriodoAcesso> response = cliente.target(TestPortProvider.generateURL("/periodoAcesso")).path("/buscar").request().post(Entity.entity(periodoAcesso, MediaType.APPLICATION_JSON), Collection.class);
		
		Collection<PeriodoAcesso> periodoAcessos = periodoAcessoDao.buscar(exemplo, new Paginacao());
		assertThat(periodoAcessos.isEmpty(), is(false));
		assertEquals(1, response.size());
	}
	
	/**
	 * Metodo responsavel por testar o metodo que insere um PeriodoAcesso no banco. 
	 * @throws Exception
	 */
	@Test
	public void persistir() throws Exception {
		Collection<PeriodoAcesso> periodoAcessos;
		PeriodoAcessoDao periodoAcessoDao = new PeriodoAcessoDao(manager);
		periodoAcessos = periodoAcessoDao.buscar(new PeriodoAcesso(), new Paginacao());
		
		assertThat(periodoAcessos.isEmpty(), is(true));
		PeriodoAcesso periodoAcesso = preencherPeriodoAcessoCadastramento();
		
		PeriodoAcesso response = cliente.target(TestPortProvider.generateURL("/periodoAcesso")).path("/persistir").request().post(Entity.entity(periodoAcesso, MediaType.APPLICATION_JSON), PeriodoAcesso.class);
		assertThat(response.getCodigo(), is(Acao.CADASTRAMENTO));
		
		periodoAcessos = periodoAcessoDao.buscar(new PeriodoAcesso(), new Paginacao());
		assertEquals(1, periodoAcessos.size());	
	}
	
	/**
	 * Metodo responsavel por testar a listagem sem nenhum PeriodoAcesso previamente cadastrado. 
	 * @throws Exception
	 */
	@Test
	public void listarVazio() throws Exception {
		PeriodoAcessoDao periodoAcessoDao = new PeriodoAcessoDao(manager);
		Collection<PeriodoAcesso> periodosAcesso = periodoAcessoDao.buscar(new PeriodoAcesso(), new Paginacao());
		assertThat(periodosAcesso.isEmpty(), is(true));
	}

	/**
	 * Metodo responsavel por testar a listagem com dois PeriodoAcesso previamente cadastrados. 
	 * @throws Exception
	 */
	@Test
	public void listar() throws Exception {
		Collection<PeriodoAcesso> periodoAcessos;
		PeriodoAcessoDao periodoAcessoDao = new PeriodoAcessoDao(manager);
		periodoAcessos = periodoAcessoDao.buscar(new PeriodoAcesso(), new Paginacao());
		
		assertThat(periodoAcessos.isEmpty(), is(true));
		
		PeriodoAcesso periodoAcesso = preencherPeriodoAcessoCadastramento();
		periodoAcessoDao.persistir(periodoAcesso);
		
		periodoAcessos = cliente.target(TestPortProvider.generateURL("/periodoAcesso")).path("/listar").request().get(Collection.class);
		assertEquals(1, periodoAcessos.size());
	}
	
	/**
	 * Metodo responsavel por testar o metodo que altera um PeriodoAcesso. 
	 * @throws Exception
	 */
	@Test
	public void alterar() throws Exception {
		PeriodoAcesso periodoAcesso = preencherPeriodoAcessoCadastramento();
		
		PeriodoAcessoDao periodoAcessoDao = new PeriodoAcessoDao(manager);
		periodoAcessoDao.persistir(periodoAcesso);
		
		PeriodoAcesso recuperado = periodoAcessoDao.obter(periodoAcesso.getCodigo());
		assertEquals(recuperado.getInicio(), this.preencherCalendar("01/01/2015 19:00"));
		assertEquals(recuperado.getTermino(), this.preencherCalendar("01/01/2015 19:50"));
		
		periodoAcesso.setInicio(this.preencherCalendar("01/01/2015 20:00"));
		periodoAcesso.setTermino(this.preencherCalendar("01/01/2015 20:50"));
		
		PeriodoAcesso response = cliente.target(TestPortProvider.generateURL("/periodoAcesso")).path("/alterar").request().put(Entity.entity(periodoAcesso, MediaType.APPLICATION_JSON), PeriodoAcesso.class);
		
		assertEquals(response.getInicio(), this.preencherCalendar("01/01/2015 20:00"));
		assertEquals(response.getTermino(), this.preencherCalendar("01/01/2015 20:50"));
	}

	/**
	 * Metodo responsavel por testar o metodo que exclui um PeriodoAcesso.
	 * @throws Exception
	 */
	@Test
	public void excluir() throws Exception {
		PeriodoAcesso periodoAcesso = preencherPeriodoAcessoCadastramento();
		PeriodoAcessoDao periodoAcessoDao = new PeriodoAcessoDao(manager);
		periodoAcessoDao.persistir(periodoAcesso);
		Collection<PeriodoAcesso> periodoAcessos;
		
		periodoAcessos = periodoAcessoDao.buscar(new PeriodoAcesso(), new Paginacao());
		assertThat(periodoAcessos.isEmpty(), is(false));
		
		Acao codigo = periodoAcesso.getCodigo();
		PeriodoAcesso response = cliente.target(TestPortProvider.generateURL("/periodoAcesso")).path("/excluir/{codigo}").resolveTemplate("codigo", codigo).request().delete(PeriodoAcesso.class);
		
		periodoAcessos = periodoAcessoDao.buscar(new PeriodoAcesso(), new Paginacao());
		assertThat(response.getCodigo(), is(Acao.CADASTRAMENTO));
		assertThat(periodoAcessos.isEmpty(), is(true));
	}

}
