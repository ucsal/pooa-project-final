package br.com.ucsal.persistencia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import br.com.ucsal.model.Produto;
import br.com.ucsal.util.ioc.Injectable;
import br.com.ucsal.util.ioc.Singleton;


@Injectable
@Singleton
public class MemoriaProdutoRepository implements ProdutoRepository<Produto, Integer>{

    private Map<Integer, Produto> produtos = new HashMap<>();
    private AtomicInteger currentId = new AtomicInteger(1);

    private static MemoriaProdutoRepository instancia;
    
    public MemoriaProdutoRepository() {
        System.out.println("MemoriaProdutoRepository instanciado");
    }
    
    
    public static synchronized MemoriaProdutoRepository getInstancia() {
    	if(instancia == null) {
    		instancia = new MemoriaProdutoRepository();
    	}
    	return instancia;
	}
    
    
    @Override
    public void adicionar(Produto entidade) {
        int id = currentId.getAndIncrement();
        entidade.setId(id);
        produtos.put(entidade.getId(), entidade);
    }
    
    @Override
    public void atualizar(Produto entidade) {
        produtos.put(entidade.getId(), entidade);
    }


    @Override
    public void remover(Integer id) {
        produtos.remove(id);
    }

    @Override
    public List<Produto> listar() {
        System.out.println("Listando produtos " + produtos.size());

        return new ArrayList<>(produtos.values());
    }

    @Override
    public Produto obterPorID(Integer id) {
        return produtos.get(id);
    }


}
