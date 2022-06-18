import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Client {

    private static Part currentPart = null;
    private static PartRepository currentRepo = null;
    private static List<PartQuant> currentSubPartsList = new LinkedList<>();
    private static Scanner scanner = new Scanner(System.in);
    private static String host = "127.0.0.1";
    private static boolean running = false;

    public enum actions {
        HELP, BIND, LISTP, GETP, SHOWP, CLIST, ADDSUBP, ADDP, QUIT;
    }

    public static void main(String[] args) throws RemoteException {
        executeClient();
    }

    private static void executeClient() throws RemoteException {
        running = true;
        System.out.println("Ola! Este eh um sistema de informacoes de pecas distribuido.");
        System.out.println("Caso precise de ajuda digite \"help\" sem as aspas.");
        while (running) {
            try {
                String action = "";
                System.out.println("O que deseja fazer?");
                action = scanner.nextLine();

                String[] actionSplit = action.split("\"");
                String[] actionSplitSpace = action.split(" ");
                actions chosenAction = actions.valueOf(actionSplitSpace[0].toUpperCase());
                
                switch (chosenAction) {
                    case HELP    -> help();
                    case BIND    -> bind(actionSplit[1]);
                    case LISTP   -> listp();
                    case GETP    -> getp(Integer.parseInt(actionSplitSpace[1]));
                    case SHOWP   -> showp();
                    case CLIST   -> clearList();
                    case ADDSUBP -> addSubPart(Integer.parseInt(actionSplitSpace[1]));
                    case ADDP    -> addPart(actionSplit[1], actionSplit[3]);
                    case QUIT    -> quit();
                }

            } catch (Exception e) {
                System.out.println("Comando invalido!");
            }
        }
    }

    private static void help() {
        System.out.println("Comandos disponíveis:\n"
        +" Obs.: As aspas duplas nos comandos BIND e ADDP sao obrigatorias,"
        +" \nenquanto as aspas simples nos demais comandos devem ser omitidas.\n\n"
        +" BIND: Usado para se conectar a um repositorio de um servidor.\n"
        +"  Sintaxe: bind \"nome do servidor\"\n\n"
        +" LISTP: Lista as pecas do repositorio atual e suas respectivas informacoes.\n"
        +"  Sintaxe: listp\n\n"
        +" GETP: Armazena a referencia de uma peca do repositorio atual em \"pecaAtual\", busca feita pelo codigo da peca.\n"
        +"  Sintaxe: getp \'codigoDaPeca\'\n\n"
        +" SHOWP: Exibe as informacoes da peca obtida com ao comando anterior.\n"
        +"  Sintaxe: showp\n\n"
        +" CLIST: Limpa a lista \"subPecas\" contidas no cliente.\n"
        +"  Sintaxe: clist\n\n"
        +" ADDSUBP: Adiciona certa quantidade da peca atual na lista \"subPecas\".\n"
        +"  Sintaxe: addsubp \'quantidade\'\n\n"
        +" ADDP: Adiciona uma peca ao repositorio atual, a lista \"subPecas\" eh usada como lista de subpecas da peca inserida.\n"
        +"  Sintaxe: addp \"nome da peca\" \"descricao da peca\"\n\n"
        +" QUIT: Termina as operacoes e o processo cliente.\n"
        +"  Sintaxe: quit");
    }

    private static void bind(String repoName) {
        try {

            int serverPort = 5000 + Integer.parseInt(repoName.split(" ")[1]);
            Registry remoteRegistry = LocateRegistry.getRegistry(host, serverPort);
            currentRepo = (PartRepository) remoteRegistry.lookup(repoName);
            System.out.println("repositorio atual: " + currentRepo.getName() + " qtd. de pecas: " + currentRepo.getPartsQuant());
            
        } catch (Exception e) {
            System.out.println("ERRO NO CLIENTE:\n" + e.toString());
        }
    }

    private static void listp() throws RemoteException {
        String header = "=============================================================================================="
        +"\n\nInfos de todas as pecas:\n\n";
        String infos = currentRepo.getAllPartsInfos();
        System.out.println(infos == "" ?
         "Repositorio vazio" : header+infos);
    }

    private static void getp(int partCode) throws RemoteException {
        currentPart = (Part) currentRepo.getPartRemoteByCode(partCode);
        System.out.println(currentPart != null ?
         "Peca obtida com sucesso! Codigo: " + partCode : "Não foi possível obter a peca de codigo: " + partCode);
    }

    private static void showp() throws RemoteException {
        String header = "=============================================================================================="
        +"\n\nInfos da peca escolhida:\n\n";
        System.out.println(currentPart != null ?
         header + currentPart.getInfo() : "Peca invalida!");
    }

    private static void clearList() {
        currentSubPartsList.clear();
        System.out.println("Lista de sub-pecas esta limpa.");
    }

    private static void addSubPart(int quant) throws RemoteException {
        System.out.println(currentSubPartsList.add((PartQuant) currentPart.createPartQuantRemote(quant)) ?
         "Sub-peca adicionada com sucesso!" : "Ocorreu um erro.");
    }

    private static void addPart(String partName, String partDesc) throws RemoteException {
        System.out.println(currentRepo.insertPart(partName, partDesc, currentRepo.getName(), currentSubPartsList) ?
         "Peca adicionada com sucesso!" : "Ocorreu um erro.");
    }

    private static void quit() {
        System.out.println("Tchau!");
    }
}