import java.util.*;
import java.io.*;
/*
�㷨����:
	1.���ļ��еõ���ʼ����ע����
	2.�Ӵ˼�����ѡ�����֧�ֶȵ����
	3.�Ӵ������ѡ��һ������Ϊǰ׺,Ȼ����ͼ��ӵ�д�ǰ׺�ĸ��߼���Ƶ���
	4.ֱ���������п���
	5.�����ҵ���Ƶ����������

notice:
	1.��¼���Ǳ���ע�б���ǹ�ע�б�;
		�����ٶ��⿪һ�����¼ÿ���˵ı���ע������;
	2.��Ƶ���ת��Ϊstring�ٱ���;
		list�޷��Ƚϴ�С,���޷�����,ȥ��,ת��string��������Դ��ıȽ���;

*/
public class Main {
	public static void main(String[] args) {
		itemset.print("----import begin----");//��������
		long start_time = System.currentTimeMillis();
		
		candi_set info=file_io.readfiles();
		
		itemset.print("time:"+(start_time=System.currentTimeMillis()-start_time)+"ms");
		itemset.print("---- import end ----");
		
		
		
		
		itemset.minsup=(int) (info.second.size()*0.01);//������С֧�ֶ�(�˴���info.second.size()����ʵ�ʵ���������,����Ϊ����ֵ)
		/*
		 * ��С֧�ֶȹ�С�Ἣ������ʱ������,������ڴ����,�뵱��
		 * */
		itemset.print("----find frequent itemset begin----");//Ѱ��Ƶ���
		start_time = System.currentTimeMillis();
		itemset.print("min-support is "+itemset.minsup);
		List<String> next_list=itemset.get_next_list(info);//�õ���ѡ����
		itemset.next_round(info,next_list);//ͨ������ͺ�ѡ���ϵݹ�Ѱ��Ƶ���
		
		itemset.print("time:"+(start_time=System.currentTimeMillis()-start_time)+"ms");
		itemset.print("---- find frequent itemset end ----");
		
		
		
		
		
		itemset.print("----output itemset begin----");
		start_time = System.currentTimeMillis();
		file_io.output_itemsets();//������ļ�
	//	itemset.print_itemsets();//���������̨
		itemset.print("get "+itemset.final_ans.size()+" itemsets, time:"+(start_time=System.currentTimeMillis()-start_time)+"ms");
		itemset.print("---- output itemset end ----");
		
		
		
	}
}





class itemset{//ȫ�ֱ���������
	public static Integer minsup;//��С֧�ֶ�,
	public static String dir = "C:\\Users\\Jin\\Desktop\\233\\apriori\\twitter\\twitter";//�����ļ���λ��
	public static Set<ans_set> final_ans=new TreeSet<ans_set>();//�洢Ƶ���������

	/*
	next_round
	��new_list��ѡ��һ��������A��Ϊ������ݹ�������Ƶ���ǰ׺,
	����ÿһ��A;
		��Ӧ����С����ע����tmp,��������ans;
		����get_next_list������ans�е�Ƶ���,��¼��final_ans,������һ������B;
		ʹ��ans��B���и����εĵݹ�;
		ֱ��BΪ��,˵�����γ��Բ����и��߼����Ƶ�����,��������һ��ݹ�;
	*/
	public static void next_round( candi_set tmp , List<String> new_list){
		
		candi_set ans;
		for(int i=0;i<new_list.size();++i){//����ѡ����Ϊ��,�򲻻����Ѱ��
			String new_item=new_list.get(i);
			ans= new candi_set(tmp.first+" "+new_item,new HashMap<String,Set<String>>());//�ݹ�Խ��,tmp.first��Ƶ�������ȷ����Ԫ��Խ��
			
			for(int j=i+1;j<new_list.size();++j){
				String n=new_list.get(j);
				Set<String> tmp_set=ans.second.get(n);
				if(tmp_set==null)
					tmp_set=new HashSet<String>();
				for(String m:tmp.second.get(new_item)){
					/*
					 * ����ѭ��:�Ӻ�ѡ����ѡ��һ����Ϊ��һ�ֵ�ȷ����;
					 * Ȼ��Ӻ�ѡ��������һ����ѡ��m
					 * ��������Ԫ��m(��һ���Ǻ�ѡ��),�ȹ�ע��new_item,�ֹ�ע��m,��˵��new_item��m�л����Ϊ������Ƶ���
					 * ���m��n��¼��һ���±�tmp_set��
					 * */
					if(tmp.second.get(n).contains(m)){
						tmp_set.add(m);
					}
				}
				ans.second.put(n,tmp_set);
			}
			List<String> next_list=get_next_list(ans);
			/*
			 * tmp_set������ɺ�,��������������������������浽final_ans,׼��������
			 * ������һ���µĺ�ѡ��
			 * */
			next_round(ans,next_list);//Я���º�ѡ�����±������һ�εݹ�
		}
		
	}
	/*
	get_next_list
	��tmp�еõ��µ�Ƶ����,��tmp��ǰ׺�ϲ�,�õ��µ�Ƶ���;
		��Ϊ�ݹ��������,��Ƶ����������,��˽�Ƶ��������ת�����ַ���,ʹ��TreeSet����ȥ��,ͬʱ����;
		����һ��Ƶ����ĸ���,������С����ע����;
	*/
	public static List<String> get_next_list(candi_set tmp){
		List<String> ans=new ArrayList<String>();
		for(Map.Entry<String, Set<String>> i:tmp.second.entrySet()){
			if(i.getValue().size()<itemset.minsup)//���ĳ����������֧�ֶ�,��������
				continue;
			
			/*
			 * ��Ƶ������򲢺ϳ�һ���ַ���,�Ա��������
			 *
			 * */
			String pre_arr[]=(tmp.first.trim()+" "+i.getKey()).trim().split(" ");
			Arrays.sort(pre_arr);
			String tmp_ans=pre_arr[0];
			for(int j=1;j<pre_arr.length;++j){
				tmp_ans+=" "+pre_arr[j];
			}
			final_ans.add(new ans_set(tmp_ans,i.getValue().size()));
			ans.add(i.getKey());//����µĺ�ѡ��
		}
		return ans;
	}
	/*
	����д��ô����������...�������˸��﷨��;
	*/
	public static void print(String a){
		System.out.println(a);
	}
	/*
	����𰸵���Ļ
	*/
	public static void print_itemsets(){//���Ƶ�����ϵ���Ļ
			for(ans_set i:itemset.final_ans){
				print("["+i.str+"] support: "+i.sup);
			}
	}
}



class file_io{
	//��д�ļ�
	
	/*
	����ָ�����ļ���
		��ȡ�ļ��������еĺ�׺Ϊedges���ļ�
		����ÿ���ļ�
			��ȡÿһ��
			����ϵ���ظ�����ӵ�����ע����
	*/
	public static candi_set readfiles(){
		int cont=0;
		candi_set all_map=new candi_set("",new HashMap<String,Set<String>>());
		File root = new File(itemset.dir);
		File[] files = root.listFiles();
		FileReader fr =null;
		BufferedReader br = null;
		for(File file:files){
			if(file.getName().substring(file.getName().lastIndexOf(".") + 1).equals("edges")==false)//�������edges�ļ�������
				continue;
			String id=file.getName().substring(0,file.getName().length()-6);//����ļ����е�userid
			try {
				fr = new FileReader(file);
				br = new BufferedReader(fr);
				String[] arr_str;
				String tmp;
				/*
				 * �˴���¼���Ǳ�˭��ע,�����ǹ�ע��˭
				 * �����ǵ��ŵ����ڿ��Կ��ٵ��ҳ���һ���֧�ֶ���������С֧�ֶȵ�
				 * 
				 * */
				while ((tmp= br.readLine()) != null) {
					arr_str=tmp.split(" ");
					Set<String> fst_set=all_map.second.get(arr_str[0]);
					if(fst_set==null)
						fst_set=new HashSet<String>();
					Set<String> snd_set=all_map.second.get(arr_str[1]);
					if(snd_set==null)
						snd_set=new HashSet<String>();
					
					
					if(!snd_set.contains(id)){
						snd_set.add(id);
						++cont;
					}
					
					if(!snd_set.contains(arr_str[0])){
						snd_set.add(arr_str[0]);
						++cont;
					}
					if(!fst_set.contains(id)){
						fst_set.add(id);
						++cont;
					}
					
					all_map.second.put(arr_str[0], fst_set);
					all_map.second.put(arr_str[1],snd_set);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  	try {
			    br.close();
			    fr.close();
			}catch (IOException e) {
			    e.printStackTrace();
			}
		}
		
		itemset.print("import "+cont+" edges");
		return all_map;
	}
	/*
	����д�������ļ����µ�frequent_itemset.ext��;
	�����ʽ���������Ļ�ĺ�����ͬ
	*/
	public static void output_itemsets(){//���Ƶ������ļ�
		
		try {
			File out_file = new File(itemset.dir+"\\frequent_itemset.txt");
			out_file.createNewFile();
			FileWriter out_writer = new FileWriter(out_file);
			BufferedWriter out_buf = new BufferedWriter(out_writer);
			for(ans_set i:itemset.final_ans){
				out_buf.write("["+i.str+"] support: "+i.sup+"\n");
			}
			out_buf.flush();
			
			out_buf.close();
			out_writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

/*
����ȥ�غ�����Ƶ������õĶ���,��д�˱ȽϺ���
*/
class ans_set implements Comparable<ans_set>{
	String str;
	Integer sup;   
    public ans_set(String a,Integer b){
    	str=a;
    	sup=b;
    }

	@Override
	public int compareTo(ans_set o) {
		int a=str.split(" ").length;
		int b=o.str.split(" ").length;
		if(a!=b)
			return a-b;
		return str.compareTo(o.str);
	}
}


/*
���ڴ��ݱ���ע���Ϻͼ�¼���Ӧǰ׺�Ķ���
*/
class candi_set{

    String first;//����ע���϶�Ӧ��ǰ׺
    Map<String,Set<String>> second;//һ������ע����;
    public candi_set() {
    	first = null;second = null;
    }
    public candi_set(String f,Map<String,Set<String>> s){
        first = f;
        second = s;
    }
}



