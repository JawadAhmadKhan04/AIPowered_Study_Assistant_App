from langchain.prompts import PromptTemplate
from langchain.chains import LLMChain
from langchain_groq import ChatGroq
from dotenv import load_dotenv
import os
import re


class SmartDigest:
    def __init__(self):
        load_dotenv()
        self.groq_api_key = os.getenv("GROQ_API_KEY")
        self.model = ChatGroq(model="Gemma2-9b-It", groq_api_key=self.groq_api_key)
        
        self.summarization_prompt = PromptTemplate(
            input_variables=["context", "text"],
            template="Context: {context}\nText: {text}\n\nSummarize the text based on the given topic. The summary must be in easy words and not more than 50 words."
            "Do not include any introductions, explanations, or extra phrases like 'Here are...' or 'The key points are...'. "
        "Just return the points directly."
        )
        self.summarization_chain = LLMChain(llm=self.model, prompt=self.summarization_prompt)
        
        self.key_points_prompt = PromptTemplate(
            input_variables=["context", "text"],
            template="Context: {context}\nText: {text}\n\nExtract key points from the text based on the given context. The key points must be in easy words and contain external info as well if it helps in understanding the concept"
            "Do not include any introductions, explanations, or extra phrases like 'Here are...' or 'The key points are...'. "
        "Just return the points directly in numbered format."
        )
        self.key_points_chain = LLMChain(llm=self.model, prompt=self.key_points_prompt)
        
        self.concept_list_prompt = PromptTemplate(
            input_variables=["context", "text"],
            template="Context: {context}\nText: {text}\n\nExtract a list of concepts from the text based on the given context. The concepts must be in easy words and might contain external info as well if it helps in understanding the concept. "
            "Respond with only the key points as plain, numbered sentences. "
        "Do not include any introductions, explanations, or extra phrases like 'Here are...' or 'The key points are...'. "
        "Just return the points directly in numbered format."
        )
        self.concept_list_chain = LLMChain(llm=self.model, prompt=self.concept_list_prompt)
        
        self.quiz_prompt = PromptTemplate(
            input_variables=["context", "text", "question_count"],
            template="Context: {context}\nText: {text}\n\nGenerate a quiz with a total of {question_count} questions 4 options (A, B, C, D) and the correct answer for each question along with its short explanation. "
            "The quiz should be based on the text and context provided. Although you may include external information if it is slightly relevant to the quiz"
            "Respond with only the quiz questions and options and answer along with it in a structured format."
            "Create me the exact number of questions i asked for. Do not create more or less than that. "
            "If for instance i ask for 50 questions then generate all 50 in one go. Dont be like 'I have only created a sample of questions' "
            "Do not include any introductions, explanations, or extra phrases like 'Here are...' or 'The quiz is...'. "
            "Remove any initial extra spa"
        )
        self.quiz_chain = LLMChain(llm=self.model, prompt=self.quiz_prompt)


    def clean_key_points(self, text):
        text = re.sub(r'\*\*(.*?)\*\*', r'\1', text)  # remove **bold**
        text = re.sub(r'\.{2,}', '.', text)           # replace .. or ... with .
        text = re.sub(r'[-*•]+', '', text)            # remove bullets like *, -, •
        return text.strip()



    def summarize_text(self, context, text):
        summary = self.summarization_chain.run({"context": context, "text": text})
        return self.clean_key_points(summary)
    
    def extract_key_points(self, context, text):
        key_points = self.key_points_chain.run({"context": context, "text": text})
        return self.clean_key_points(key_points)

    def extract_concept_list(self, context, text):
        concept_list = self.concept_list_chain.run({"context": context, "text": text})
        return self.clean_key_points(concept_list)
    
    def generate_quiz(self, context, text, question_count):
        question_count+=1
        quiz_list = self.quiz_chain.run({"context": context, "text": text, "question_count": question_count})
        print(f"\n\n\n\n\n{quiz_list[0:-1]}\n\n\n\n")
        return self.clean_key_points(quiz_list)
    
    
    def parse_quiz_to_json(self, quiz_text):
        questions = re.split(r'\nQuestion \d+\n', quiz_text)
        questions = [q.strip() for q in questions if q.strip()]
        
        quiz_list = []
        questions = questions[1:]  # Skip the first element which is empty or not a question
        for q in questions:
            lines = q.split('\n')
            question = lines[0]
            options = {}
            answer = ""
            explanation = ""

            for line in lines[1:]:
                if line.startswith("A."):
                    options["optionA"] = line[2:].strip()
                elif line.startswith("B."):
                    options["optionB"] = line[2:].strip()
                elif line.startswith("C."):
                    options["optionC"] = line[2:].strip()
                elif line.startswith("D."):
                    options["optionD"] = line[2:].strip()
                elif line.startswith("Answer:"):
                    answer = line.split("Answer:")[1].strip()
                elif line.startswith("Explanation:"):
                    explanation = line.split("Explanation:")[1].strip()
            
            quiz_list.append({
                "question": question,
                "A": options.get("optionA", ""),
                "B": options.get("optionB", ""),
                "C": options.get("optionC", ""),
                "D": options.get("optionD", ""),
                "answer": answer,
                "explanation": explanation
            })
            
        print(quiz_list)

        return quiz_list

    
    # def generate_quiz(self, context, text, question_count):
    #     quiz_list = []
    #     for i in range(question_count):
    #         data = {
    #             "Q": f"Question{i+1}",
    #             "A": "OptionA",
    #             "B": "OptionB",
    #             "C": "OptionC",
    #             "D": "OptionD",
    #             "Answer": "C"
    #         }
    #         quiz_list.append(data)
    #     return quiz_list