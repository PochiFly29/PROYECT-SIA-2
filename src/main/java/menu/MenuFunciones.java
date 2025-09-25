    package menu;

    import gestores.GestorIntercambio;
    import modelo.Usuario;
    import servicios.VerificarInput;
    import enums.Rol;
/*
    public class MenuFunciones {

        private final VerificarInput input;
        private final GestorIntercambio gestor;
        private final Usuario usuarioActual;
        private final MenuConvenios menuConvenios;
        private final MenuPostulaciones menuPostulaciones;
        private final MenuPerfil menuPerfil;

        public MenuFunciones(VerificarInput input, GestorIntercambio gestor, Usuario usuarioActual,
                             MenuConvenios menuConvenios, MenuPostulaciones menuPostulaciones, MenuPerfil menuPerfil) {
            this.input = input;
            this.gestor = gestor;
            this.usuarioActual = usuarioActual;
            this.menuConvenios = menuConvenios;
            this.menuPostulaciones = menuPostulaciones;
            this.menuPerfil = menuPerfil;
        }

        public void ejecutarMenu() {
            while (true) {
                System.out.println("\n=== Menú de " + usuarioActual.getRol() + " ===");
                int op = -1;

                if (usuarioActual.getRol() == Rol.ESTUDIANTE) {
                    mostrarOpcionesEstudiante();
                    op = input.leerEntero("Opción: ", -1);
                    if (op == 0) return;
                    procesarOpcionEstudiante(op);

                } else if (usuarioActual.getRol() == Rol.FUNCIONARIO) {
                    mostrarOpcionesFuncionario();
                    op = input.leerEntero("Opción: ", -1);
                    if (op == 0) return;
                    procesarOpcionFuncionario(op);

                } else if (usuarioActual.getRol() == Rol.AUDITOR) {
                    mostrarOpcionesAuditor();
                    op = input.leerEntero("Opción: ", -1);
                    if (op == 0) return;
                    procesarOpcionAuditor(op);
                }
            }
        }

        private void mostrarOpcionesEstudiante() {
            System.out.println("1) Ver mi Perfil");
            System.out.println("2) Ver mis Postulaciones");
            System.out.println("3) Postular a un Convenio");
        }

        private void mostrarOpcionesFuncionario() {
            System.out.println("1) Ver mi Perfil");
            System.out.println("2) Revisar Postulaciones");
            System.out.println("3) Catalogo de convenios");
        }

        private void procesarOpcionEstudiante(int op) {
            switch (op) {
                case 1:
                    this.menuPerfil.ejecutarMenu();
                    break;
                case 2:
                    this.menuConvenios.ejecutarMenu();
                    break;
                case 3:
                    this.menuPostulaciones.ejecutarMenuEstudiante();
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }

        private void procesarOpcionFuncionario(int op) {
            switch (op) {
                case 1:
                    this.menuPerfil.ejecutarMenu();
                    break;
                case 2:
                    this.menuConvenios.ejecutarMenuFuncionario();
                    break;
                case 3:
                    this.menuPostulaciones.ejecutarMenuFuncionario();
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }
 */