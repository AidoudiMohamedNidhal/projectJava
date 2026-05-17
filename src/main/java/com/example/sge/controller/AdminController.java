package com.example.sge.controller;

import com.example.sge.model.Etudiant;
import com.example.sge.model.Filiere;
import com.example.sge.model.Module;
import com.example.sge.model.Note;
import com.example.sge.repository.EtudiantRepository;
import com.example.sge.repository.FiliereRepository;
import com.example.sge.repository.ModuleRepository;
import com.example.sge.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private FiliereRepository filiereRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private EtudiantRepository etudiantRepository;

    @Autowired
    private NoteRepository noteRepository;

    // Dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalFilieres", filiereRepository.count());
        model.addAttribute("totalModules", moduleRepository.count());
        model.addAttribute("totalEtudiants", etudiantRepository.count());
        model.addAttribute("totalNotes", noteRepository.count());
        return "admin/dashboard";
    }

    // Filiere Management
    @GetMapping("/filieres")
    public String filieres(Model model) {
        List<Filiere> filieres = filiereRepository.findAll();
        model.addAttribute("filieres", filieres);
        return "admin/filieres";
    }

    @PostMapping("/filieres/add")
    public String addFiliere(
            @RequestParam String nom,
            @RequestParam String niveau,
            @RequestParam Integer capacite) {
        Filiere filiere = new Filiere();
        filiere.setNom(nom);
        filiere.setNiveau(niveau);
        filiere.setCapacite(capacite);
        filiereRepository.save(filiere);
        return "redirect:/admin/filieres";
    }

    @GetMapping("/filieres/delete/{id}")
    public String deleteFiliere(@PathVariable Long id) {
        filiereRepository.deleteById(id);
        return "redirect:/admin/filieres";
    }

    // Module Management
    @GetMapping("/modules")
    public String modules(Model model) {
        List<Module> modules = moduleRepository.findAll();
        List<Filiere> filieres = filiereRepository.findAll();
        model.addAttribute("modules", modules);
        model.addAttribute("filieres", filieres);
        return "admin/modules";
    }

    @PostMapping("/modules/add")
    public String addModule(
            @RequestParam String nom,
            @RequestParam String code,
            @RequestParam Integer coefficient,
            @RequestParam Long filiereId) {
        Filiere filiere = filiereRepository.findById(filiereId)
                .orElseThrow(() -> new RuntimeException("Filière non trouvée"));
        Module module = new Module();
        module.setNom(nom);
        module.setCode(code);
        module.setCoefficient(coefficient);
        module.setFiliere(filiere);
        moduleRepository.save(module);
        return "redirect:/admin/modules";
    }

    @GetMapping("/modules/delete/{id}")
    public String deleteModule(@PathVariable Long id) {
        moduleRepository.deleteById(id);
        return "redirect:/admin/modules";
    }

    // Etudiant Management
    @GetMapping("/etudiants")
    public String etudiants(Model model) {
        List<Etudiant> etudiants = etudiantRepository.findAll();
        List<Filiere> filieres = filiereRepository.findAll();
        model.addAttribute("etudiants", etudiants);
        model.addAttribute("filieres", filieres);
        return "admin/etudiants";
    }

    @PostMapping("/etudiants/add")
    public String addEtudiant(
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String email,
            @RequestParam String cin,
            @RequestParam String dateNaissance,
            @RequestParam String groupe,
            @RequestParam Long filiereId) {
        Filiere filiere = filiereRepository.findById(filiereId)
                .orElseThrow(() -> new RuntimeException("Filière non trouvée"));
        Etudiant etudiant = new Etudiant();
        etudiant.setNom(nom);
        etudiant.setPrenom(prenom);
        etudiant.setEmail(email);
        etudiant.setCin(cin);
        etudiant.setDateNaissance(LocalDate.parse(dateNaissance));
        etudiant.setGroupe(groupe);
        etudiant.setFiliere(filiere);
        etudiant.setMoyenne(0.0);
        etudiantRepository.save(etudiant);
        return "redirect:/admin/etudiants";
    }

    @GetMapping("/etudiants/delete/{id}")
    public String deleteEtudiant(@PathVariable Long id) {
        etudiantRepository.deleteById(id);
        return "redirect:/admin/etudiants";
    }

    // Note Management
    @GetMapping("/notes")
    public String notes(Model model) {
        List<Note> notes = noteRepository.findAll();
        List<Etudiant> etudiants = etudiantRepository.findAll();
        List<Module> modules = moduleRepository.findAll();
        model.addAttribute("notes", notes);
        model.addAttribute("etudiants", etudiants);
        model.addAttribute("modules", modules);
        return "admin/notes";
    }

    @PostMapping("/notes/add")
    public String addNote(
            @RequestParam Long etudiantId,
            @RequestParam Long moduleId,
            @RequestParam Double valeur) {
        Etudiant etudiant = etudiantRepository.findById(etudiantId)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));
        Note note = new Note();
        note.setValeur(valeur);
        note.setEtudiant(etudiant);
        note.setModule(module);
        noteRepository.save(note);

        // Update student average
        Double moyenne = noteRepository.calculerMoyenne(etudiantId);
        if (moyenne != null) {
            etudiant.setMoyenne(moyenne);
            etudiantRepository.save(etudiant);
        }

        return "redirect:/admin/notes";
    }

    @GetMapping("/notes/delete/{id}")
    public String deleteNote(@PathVariable Long id) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note != null) {
            Long etudiantId = note.getEtudiant().getId();
            noteRepository.deleteById(id);

            // Update student average
            Double moyenne = noteRepository.calculerMoyenne(etudiantId);
            Etudiant etudiant = etudiantRepository.findById(etudiantId).orElse(null);
            if (etudiant != null) {
                etudiant.setMoyenne(moyenne != null ? moyenne : 0.0);
                etudiantRepository.save(etudiant);
            }
        }
        return "redirect:/admin/notes";
    }
}
