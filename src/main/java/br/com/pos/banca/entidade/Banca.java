package br.com.pos.banca.entidade;

import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import br.com.pos.academico.entidade.Pessoa;
import br.com.pos.banca.constante.TipoBanca;

@EqualsAndHashCode(of = "codigo")
public class Banca {
	
	@Getter @Setter
	private Integer codigo;
	
	@Getter @Setter
	private TipoBanca tipo;
	
	@Getter @Setter
	private Trabalho trabalho;
	
	@Getter @Setter
	private Set<Pessoa> membros;
	
	public Banca() {
		
	}
	
	public boolean adicionarMembro(Pessoa pessoa) {
		return membros.add(pessoa);
	}

}
