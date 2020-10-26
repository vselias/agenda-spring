package com.example.app.controller;

import java.io.IOException;
import java.util.Date;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.app.dao.DocDAO;
import com.example.app.entidade.Doc;
import com.example.app.entidade.Pessoa;
import com.example.app.service.PessoaService;

@Controller
public class PessoaController {

	@Autowired
	private PessoaService pessoaService;

	@Autowired
	private DocDAO docDAO;

	@GetMapping("/pessoa")
	public Pessoa pessoa(@RequestParam(value = "nome") String nome, @RequestParam(value = "tel") String telefone) {
		return new Pessoa(2123223L, nome, telefone);
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("pessoa", new Pessoa());
		return "index";
	}

	@GetMapping("/index")
	public String index(Model model) {
		model.addAttribute("pessoa", new Pessoa());
		return "index";
	}

	@GetMapping("/cadastro")
	public String cadastro(Model model) {
		model.addAttribute("pessoa", new Pessoa());
		return "cadastro";
	}

	@GetMapping("/pessoas")
	public String pessoas(Model model) {
		model.addAttribute("pessoas", pessoaService.findAll());
		return "pessoas";
	}

	@GetMapping("/pessoas/{page}")
	public String pessoasPaginacao(@PathVariable(value = "page") int page, Model model) {
		Page<Pessoa> pessoas = pessoaService.findPaging(page);
		model.addAttribute("pessoas", pessoas.getContent());
		model.addAttribute("numPaginas", pessoas.getTotalPages());
		model.addAttribute("ativo", page);
		return "pessoas";
	}

	@PostMapping("/salvar")
	public String salvar(@Valid Pessoa pessoa, BindingResult bindingResult, Model model,
			@RequestParam("arquivos") MultipartFile[] files) throws IOException {

		if (pessoa.getId() != null) {
			Pessoa findById = pessoaService.findById(pessoa.getId());
			if (!pessoa.getEmail().equalsIgnoreCase(findById.getEmail())) {
				if (bindingResult.hasFieldErrors("email")) {
					model.addAttribute("msgEmail", true);
					return "cadastro";
				}
			}
			for (FieldError field : bindingResult.getFieldErrors()) {
				System.out.println(field.getCode());
				if (!field.getCode().equals("UniqueEmail"))
					return "cadastro";
			}
		} else {
			if (bindingResult.hasErrors()) {
				return "cadastro";
			}
		}

		model.addAttribute("msgCadastro", pessoa.getId() == null ? "Cadastrado!" : "Atualizado!");
		pessoaService.salvar(pessoa, files);
		return "cadastro";
	}

	@GetMapping("/edit/{id}")
	public String edit(@PathVariable(name = "id") Long id, Model model) {
		Pessoa pessoa = pessoaService.findById(id);
		model.addAttribute("pessoa", pessoa);
		System.out.println(pessoa.toString());
		return "cadastro";
	}

	@GetMapping("/download/{id}")
	public ResponseEntity<Resource> downloadFile(@PathVariable(value = "id") long id) {
		Doc doc = docDAO.findById(id).get();

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(doc.getTipoArquivo()))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + doc.getNomeArquivo() + "\"")
				.body(new ByteArrayResource(doc.getData()));

	}

	@GetMapping("/del/{id}")
	public String delete(@PathVariable(name = "id") Long id, RedirectAttributes ra) {
		pessoaService.delete(pessoaService.findById(id));
		ra.addFlashAttribute("msgDelete", "Removido!");
		return "redirect:/pessoas/1";
	}

	@GetMapping("/senha")
	public String senha() {
		String senha = new BCryptPasswordEncoder().encode("321");
		System.out.printf(senha);
		return "login";
	}

	@GetMapping("/sort/{tipo}")
	public String sort(@PathVariable(name = "tipo") String tipo, Model model, @RequestParam("pag") int pag,
			@RequestParam(name = "ordem", defaultValue = "asc") String ordem) {
		Page<Pessoa> sortPorTipo = pessoaService.sortPorTipo(tipo, ordem, pag);
		model.addAttribute("revDir", ordem.equals("asc") ? "desc" : "asc");
		model.addAttribute("pessoas", sortPorTipo.getContent());
		model.addAttribute("numPaginas", sortPorTipo.getTotalPages());
		model.addAttribute("ativo", pag);
		return "pessoas";
	}

	@GetMapping("/login")
	public String login() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			return "login";
		}

		return "redirect:/index";
	}

	@GetMapping("/user")
	@ResponseBody
	public String criarUser() {
		try {
			Pessoa p = new Pessoa();
			p.setNome("Admin");
			p.setEmail("user@gmail.com");
			p.setAtivo(true);
			p.setDataCadastro(new Date());
			p.setSexo("M");
			p.setSenha("321");
			pessoaService.salvar(p, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Usuário já criado!";
	}
}
